package to.etc.domui.component.meta.impl;

import java.beans.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

public class DefaultPropertyMetaModel extends BasicPropertyMetaModel implements PropertyMetaModel {
	private final DefaultClassMetaModel m_classModel;

	private final PropertyDescriptor m_descriptor;

	private final PropertyAccessor< ? > m_accessor;

	private int m_length = -1;

	private boolean m_primaryKey;

	private PropertyRelationType m_relationType = PropertyRelationType.NONE;

	private String m_componentTypeHint;

	private String m_renderHint;

	/**
	 * If this class is the UP in a relation this specifies that it must
	 * be shown as a COMBOBOX containing choices. It contains a generator
	 * for the values to show. This is a default for all relations in which
	 * this class is the parent; it can be overridden in individual relations.
	 */
	private Class< ? extends IComboDataSet< ? >> m_comboDataSet;

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	private Class< ? extends ILabelStringRenderer< ? >> m_comboLabelRenderer;

	private Class< ? extends INodeContentRenderer< ? >> m_comboNodeRenderer;

	private List<DisplayPropertyMetaModel> m_comboDisplayProperties = Collections.EMPTY_LIST;

	/*---- Lookup stuff. ----*/

	/**
	 * Default renderer which renders a lookup field's "field" contents; this is a table which must be filled with
	 * data pertaining to the looked-up item as a single element on the "edit" screen.
	 */
	private Class< ? extends INodeContentRenderer< ? >> m_lookupFieldRenderer;

	/**
	 * The default properties to show in a lookup field's instance display.
	 */
	private List<DisplayPropertyMetaModel> m_lookupFieldDisplayProperties = Collections.EMPTY_LIST;

	/**
	 * The default properties to show when the collection is presented as a lookup table.
	 */
	private List<DisplayPropertyMetaModel> m_tableDisplayProperties = Collections.EMPTY_LIST;


	public DefaultPropertyMetaModel(final DefaultClassMetaModel classModel, final PropertyDescriptor descriptor) {
		m_classModel = classModel;
		if(classModel == null)
			throw new IllegalStateException("Cannot be null dude");
		m_descriptor = descriptor;
		m_accessor = new PropertyAccessor<Object>(descriptor.getReadMethod(), descriptor.getWriteMethod(), this);
		if(descriptor.getWriteMethod() == null) {
			setReadOnly(YesNoType.YES);
		}

		Annotation[] annar = descriptor.getReadMethod().getAnnotations();
		for(Annotation an : annar) {
			String ana = an.annotationType().getName();
			decodeAnnotationByName(an, ana);
			decodeAnnotation(an);
		}
	}

	@SuppressWarnings({"cast", "unchecked"})
	protected void decodeAnnotation(final Annotation an) {
		if(an instanceof MetaProperty) {
			//-- Handle meta-assignments.
			MetaProperty mp = (MetaProperty) an;
			if(mp.defaultSortable() != SortableType.UNKNOWN)
				setSortable(mp.defaultSortable());
			if(mp.length() >= 0)
				m_length = mp.length();
			if(mp.displaySize() >= 0)
				setDisplayLength(mp.displaySize());
			if(mp.required() != YesNoType.UNKNOWN)
				setRequired(mp.required() == YesNoType.YES);
			if(mp.converterClass() != DummyConverter.class)
				setConverter((IConverter)ConverterRegistry.getConverterInstance((Class)mp.converterClass()));
			if(mp.editpermissions().length != 0)
				setEditRoles(makeRoleSet(mp.editpermissions()));
			if(mp.viewpermissions().length != 0)
				setViewRoles(makeRoleSet(mp.viewpermissions()));
			if(mp.temporal() != TemporalPresentationType.UNKNOWN && getTemporal() == TemporalPresentationType.UNKNOWN)
				setTemporal(mp.temporal());
			if(mp.numericPresentation() != NumericPresentation.UNKNOWN)
				setNumericPresentation(mp.numericPresentation());
			if(getReadOnly() != YesNoType.YES) // Do not override readonlyness from missing write method
				setReadOnly(mp.readOnly());
			if(mp.componentTypeHint().length() != 0)
				setComponentTypeHint(mp.componentTypeHint());

			//-- Convert validators.
			List<MetaPropertyValidatorImpl> list = new ArrayList<MetaPropertyValidatorImpl>();
			for(Class< ? extends IValueValidator< ? >> vv : mp.validator()) {
				MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(vv);
				list.add(vi);
			}
			for(MetaValueValidator mvv : mp.parameterizedValidator()) {
				MetaPropertyValidatorImpl vi = new MetaPropertyValidatorImpl(mvv.validator(), mvv.parameters());
				list.add(vi);
			}
			setValidators(list.toArray(new PropertyMetaValidator[list.size()]));

			//-- Regexp validators.
			if(mp.regexpValidation().length() > 0) {
				try {
					//-- Precompile to make sure it's valid;
					Pattern p = Pattern.compile(mp.regexpValidation());
				} catch(Exception x) {
					throw new MetaModelException(Msgs.MM_BAD_REGEXP, mp.regexpValidation(), this.toString());
				}
				setRegexpValidator(mp.regexpValidation());
				if(mp.regexpUserString().length() > 0)
					setRegexpUserString(mp.regexpUserString());
			}
		} else if(an instanceof MetaCombo) {
			MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class) {
				setRelationType(PropertyRelationType.UP);
				setComboDataSet(c.dataSet());
			}
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class) {
				setRelationType(PropertyRelationType.UP);
				setComboLabelRenderer(c.labelRenderer());
			}
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class) {
				setRelationType(PropertyRelationType.UP);
				setComboNodeRenderer(c.nodeRenderer());
			}
			if(c.optional() != ComboOptionalType.INHERITED)
				setRequired(c.optional() == ComboOptionalType.REQUIRED);
			if(c.properties() != null && c.properties().length > 0) {
				setRelationType(PropertyRelationType.UP);
				m_comboDisplayProperties = DisplayPropertyMetaModel.decode(m_classModel, c.properties());
			}
			setComponentTypeHint(Constants.COMPONENT_COMBO);
		} else if(an instanceof SearchProperty) {
			SearchProperty sp = (SearchProperty) an;
			SearchPropertyMetaModelImpl mm = new SearchPropertyMetaModelImpl((DefaultClassMetaModel) getClassModel());
			mm.setIgnoreCase(sp.ignoreCase());
			mm.setOrder(sp.order());
			mm.setMinLength(sp.minLength());
			mm.setPropertyName(getName());
			//			mm.setProperty(this);
			((DefaultClassMetaModel) getClassModel()).addSearchProperty(mm);
		} else if(an instanceof MetaObject) {
			MetaObject o = (MetaObject) an;
			if(o.defaultColumns().length > 0) {
				m_tableDisplayProperties = DisplayPropertyMetaModel.decode(m_classModel, o.defaultColumns());
			}
		} else if(an instanceof MetaLookup) {
			MetaLookup c = (MetaLookup) an;
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				m_lookupFieldRenderer = c.nodeRenderer();
			if(c.properties().length != 0)
				m_lookupFieldDisplayProperties = DisplayPropertyMetaModel.decode(m_classModel, c.properties());
			setComponentTypeHint(Constants.COMPONENT_LOOKUP);
		}
	}

	protected void decodeAnnotationByName(final Annotation an, final String name) {
		if("javax.persistence.Column".equals(name)) {
			decodeJpaColumn(an);
		} else if("javax.persistence.Id".equals(name)) {
			m_primaryKey = true;
			m_classModel.setPersistentClass(true);
		} else if("javax.persistence.ManyToOne".equals(name)) {
			setRelationType(PropertyRelationType.UP);

			//-- Decode fields from the annotation.
			try {
				Boolean op = (Boolean) DomUtil.getClassValue(an, "optional");
				setRequired(!op.booleanValue());
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		} else if("javax.persistence.Temporal".equals(name)) {
			try {
				Object val = DomUtil.getClassValue(an, "value");
				if(val != null) {
					String s = val.toString();
					if("DATE".equals(s))
						setTemporal(TemporalPresentationType.DATE);
					else if("TIME".equals(s))
						setTemporal(TemporalPresentationType.TIME);
					else if("TIMESTAMP".equals(s))
						setTemporal(TemporalPresentationType.DATETIME);
				}
			} catch(Exception x) {
				Trouble.wrapException(x);
			}
		} else if("javax.persistence.Transient".equals(name)) {
			setTransient(true);
		} else if("javax.persistence.OneToMany".equals(name)) {
			//-- This must be a list
			if(!Collection.class.isAssignableFrom(getActualType()))
				throw new IllegalStateException("Invalid property type for DOWN relation of property " + this + ": only List<T> is allowed");
			setRelationType(PropertyRelationType.DOWN);
		}
	}

	/**
	 * Generically decode a JPA javax.persistence.Column annotation.
	 * @param an
	 */
	private void decodeJpaColumn(final Annotation an) {
		try {
			/*
			 * Handle the "length" annotation. As usual, someone with a brain the size of a pea fucked up the standard. The
			 * default value for the length is 255, which is of course a totally reasonable size. This makes it impossible to
			 * see if someone has actually provided a value. This means that in absence of the length the field is fucking
			 * suddenly 255 characters big. To prevent this utter disaster from fucking up the data we only accept this for
			 * STRING fields.
			 */
			Integer iv = (Integer) DomUtil.getClassValue(an, "length");
			m_length = iv.intValue();
			if(m_length == 255) { // Idiot value?
				if(getActualType() != String.class)
					m_length = -1;
			}

			Boolean bv = (Boolean) DomUtil.getClassValue(an, "nullable");
			setRequired(!bv.booleanValue());
			iv = (Integer) DomUtil.getClassValue(an, "precision");
			setPrecision(iv.intValue());
			iv = (Integer) DomUtil.getClassValue(an, "scale");
			setScale(iv.intValue());
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new WrappedException(x);
		}
	}

	public String getName() {
		return m_descriptor.getName();
	}

	public Class< ? > getActualType() {
		return m_descriptor.getPropertyType();
	}

	public Type getGenericActualType() {
		Method m = m_descriptor.getReadMethod();
		if(m != null) {
			return m.getGenericReturnType();
		}
		m = m_descriptor.getWriteMethod();
		if(m != null) {
			return m.getGenericParameterTypes()[0];
		}
		return null;
	}

	public String getDefaultLabel() {
		return m_classModel.getPropertyLabel(this, NlsContext.getLocale());
	}

	public String getDefaultHint() {
		return m_classModel.getPropertyHint(this, NlsContext.getLocale());
	}

	static private final Object[] BOOLS = {Boolean.FALSE, Boolean.TRUE};

	/**
	 * FIXME Needs to be filled in by some kind of factory, not in this thingy directly!!
	 * For enum and boolean property types this returns the possible values for the domain. Booleans
	 * always return Boolean.TRUE and Boolean.FALSE; enums return all enum values.
	 * @return
	 */
	public Object[] getDomainValues() {
		if(getActualType() == Boolean.TYPE || getActualType() == Boolean.class) {
			return BOOLS;
		}
		if(Enum.class.isAssignableFrom(getActualType())) {
			Class< ? > ec = getActualType();
			return ec.getEnumConstants();
		}
		return null;
	}

	/**
	 * Get a property-related translation for a domain value for this property.
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDomainValueLabel(java.util.Locale, java.lang.Object)
	 */
	public String getDomainValueLabel(final Locale loc, final Object val) {
		BundleRef b = m_classModel.getClassBundle();
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(".");
		if(val == Boolean.TRUE)
			sb.append("true");
		else if(val == Boolean.FALSE)
			sb.append("false");
		else if(val instanceof Enum< ? >)
			sb.append(((Enum< ? >) val).name());
		else
			throw new IllegalStateException("Property value " + val + " for property " + this + " is not an enumerable or boolean domain");
		sb.append(".label");

		return b.findMessage(loc, sb.toString()); // jal 20081201 Do not lie about a resource based name!!
	}

	public int getLength() {
		return m_length;
	}

	/**
	 * The thingy to access the property generically.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAccessor()
	 */
	public IValueAccessor< ? > getAccessor() {
		return m_accessor;
	}

	public boolean isPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(final boolean primaryKey) {
		m_primaryKey = primaryKey;
	}

	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_comboDataSet;
	}

	public void setComboDataSet(final Class< ? extends IComboDataSet< ? >> comboDataSet) {
		m_comboDataSet = comboDataSet;
	}

	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_comboLabelRenderer;
	}

	public void setComboLabelRenderer(final Class< ? extends ILabelStringRenderer< ? >> comboLabelRenderer) {
		m_comboLabelRenderer = comboLabelRenderer;
	}

	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_comboDisplayProperties;
	}

	public void setComboDisplayProperties(final List<DisplayPropertyMetaModel> displayProperties) {
		m_comboDisplayProperties = displayProperties;
	}

	public PropertyRelationType getRelationType() {
		return m_relationType;
	}

	public void setRelationType(final PropertyRelationType relationType) {
		m_relationType = relationType;
	}

	public ClassMetaModel getClassModel() {
		return m_classModel;
	}

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_comboNodeRenderer;
	}

	public void setComboNodeRenderer(final Class< ? extends INodeContentRenderer< ? >> comboNodeRenderer) {
		m_comboNodeRenderer = comboNodeRenderer;
	}

	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_tableDisplayProperties;
	}

	public void setTableDisplayProperties(final List<DisplayPropertyMetaModel> tableDisplayProperties) {
		m_tableDisplayProperties = tableDisplayProperties;
	}

	@Override
	public String toString() {
		return getClassModel().getActualClass().getName() + "." + m_descriptor.getName() + "[" + getActualType().getName() + "]";
	}

	/**
	 * Decode stringset containing xxx+xxx to duparray.
	 * @param s
	 * @return
	 */
	static private String[][] makeRoleSet(final String[] sar) {
		if(sar.length == 0)
			return null;
		String[][] mapset = new String[sar.length][];
		ArrayList<String> al = new ArrayList<String>(10);
		int ix = 0;
		for(String s : sar) {
			StringTokenizer st = new StringTokenizer(s, ";+ \t,");
			al.clear();
			while(st.hasMoreElements()) {
				al.add(st.nextToken());
			}
			mapset[ix] = al.toArray(new String[al.size()]);
		}
		return mapset;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_lookupFieldRenderer;
	}

	public void setLookupFieldRenderer(final Class< ? extends INodeContentRenderer< ? >> lookupFieldRenderer) {
		m_lookupFieldRenderer = lookupFieldRenderer;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_lookupFieldDisplayProperties;
	}

	public void setLookupFieldDisplayProperties(final List<DisplayPropertyMetaModel> lookupFieldDisplayProperties) {
		m_lookupFieldDisplayProperties = lookupFieldDisplayProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getComponentTypeHint() {
		return m_componentTypeHint;
	}

	public void setComponentTypeHint(final String componentTypeHint) {
		m_componentTypeHint = componentTypeHint;
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(final String renderHint) {
		m_renderHint = renderHint;
	}
}
