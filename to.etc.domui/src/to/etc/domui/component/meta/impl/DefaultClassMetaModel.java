package to.etc.domui.component.meta.impl;

import java.beans.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

public class DefaultClassMetaModel implements ClassMetaModel {
	static private final ResourceBundle NONE = new ResourceBundle() {
		@Override
		protected Object handleGetObject(final String key) {
			return null;
		}

		@Override
		public Enumeration<String> getKeys() {
			return null;
		}
	};

	private final Class< ? > m_metaClass;

	private boolean m_initialized;

	private final Map<String, PropertyMetaModel> m_propertyMap = new HashMap<String, PropertyMetaModel>();

	private final Map<Locale, ResourceBundle> m_textMap = new HashMap<Locale, ResourceBundle>();

	/**
	 * When this object type is defined in an UP relation somewhere, this is a hint on what
	 * component to use. The hint is handled by the component factories.
	 */
	private String m_componentTypeHint;

	/**
	 * If this class is the UP in a relation this specifies that it must
	 * be shown as a COMBOBOX containing choices. It contains a generator
	 * for the values to show. This is a default for all relations in which
	 * this class is the parent; it can be overridden in individual relations.
	 */
	private Class< ? extends IComboDataSet< ? >> m_comboDataSet;

	private boolean m_persistentClass;

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	private Class< ? extends ILabelStringRenderer< ? >> m_comboLabelRenderer;

	private Class< ? extends INodeContentRenderer< ? >> m_comboNodeRenderer;

	private ComboOptionalType m_comboOptional;

	private List<DisplayPropertyMetaModel> m_comboDisplayProperties = Collections.EMPTY_LIST;

	private List<DisplayPropertyMetaModel> m_tableDisplayProperties = Collections.EMPTY_LIST;

	private List<SearchPropertyMetaModel> m_searchProperties = Collections.EMPTY_LIST;

	/**
	 * Default renderer which renders a lookup field's "field" contents; this is a table which must be filled with
	 * data pertaining to the looked-up item as a single element on the "edit" screen.
	 */
	private Class< ? extends INodeContentRenderer< ? >> m_lookupFieldRenderer;

	/**
	 * The default properties to show in a lookup field's instance display.
	 */
	private List<DisplayPropertyMetaModel> m_lookupFieldDisplayProperties = Collections.EMPTY_LIST;

	private String m_defaultSortProperty;

	private SortableType m_defaultSortDirection;

	private PropertyMetaModel m_primaryKey;

	private String m_userName;

	private String m_userNamePlural;

	private Object[] m_domainValues;

	public DefaultClassMetaModel(final Class< ? > metaClass) {
		m_metaClass = metaClass;
	}

	/**
	 * Decodes all properties and retrieves all known info from them.
	 */
	synchronized void initialize() {
		decodeClassAnnotations();

		try {
			BeanInfo bi = Introspector.getBeanInfo(m_metaClass);
			PropertyDescriptor[] ar = bi.getPropertyDescriptors();

			//-- If this is an enumerable thingerydoo...
			if(m_metaClass == Boolean.class) {
				m_domainValues = new Object[]{Boolean.FALSE, Boolean.TRUE};
			} else if(Enum.class.isAssignableFrom(m_metaClass)) {
				Class<Enum< ? >> ecl = (Class<Enum< ? >>) m_metaClass;
				m_domainValues = ecl.getEnumConstants();
			}

			//-- Create model data from this thingy.

			for(PropertyDescriptor pd : ar) {
				createPropertyInfo(pd);
			}
		} catch(IntrospectionException x) {
			throw new WrappedException(x);
		}
	}

	private void createPropertyInfo(final PropertyDescriptor pd) {
		//		System.out.println("Property: "+pd.getName()+", reader="+pd.getReadMethod());
		Method rm = pd.getReadMethod();
		if(rm == null) {
			//-- Handle 'isXxxx()' getters because those morons at Sun *still* don't get it.
			StringBuilder sb = new StringBuilder();
			sb.append("is");
			String s = pd.getName();
			if(s.length() > 2 && Character.isUpperCase(s.charAt(1)))
				sb.append(s);
			else {
				sb.append(Character.toUpperCase(s.charAt(0)));
				sb.append(s, 1, s.length());
			}
			s = sb.toString();

			try {
				rm = getActualClass().getMethod(s, (Class[]) null);
			} catch(Exception x) {}
			if(rm == null) // If there's no READ method here just ignore it? This is the case for getters like getChild(int ix) which are stupidly seen as array getters.
				return;
			//				throw new IllegalStateException("The 'read' method for property "+pd.getName()+" of class "+this+" is not present!?");
			try {
				pd.setReadMethod(rm);
			} catch(IntrospectionException x) {
				throw new WrappedException("Unexpected exception out of very dumb Sun interface: " + x, x);
			}
		}
		if(pd.getReadMethod().getParameterTypes().length != 0)
			return;
		DefaultPropertyMetaModel pm = new DefaultPropertyMetaModel(this, pd);
		m_propertyMap.put(pm.getName(), pm);
		if(pm.isPrimaryKey())
			m_primaryKey = pm;
	}

	/**
	 * Walk all known class annotations and use them to add class based metadata.
	 */
	protected void decodeClassAnnotations() {
		Annotation[] annar = m_metaClass.getAnnotations(); // All class-level thingerydoos
		for(Annotation an : annar) {
			String ana = an.annotationType().getName(); // Get the annotation's name
			decodeAnnotationByName(an, ana); // Decode by name literal
			decodeAnnotation(an); // Decode well-known annotations
		}
	}

	/**
	 * Can be overridden to decode user-specific annotations. The default implementation does nothing.
	 * @param an
	 * @param name
	 */
	protected void decodeAnnotationByName(final Annotation an, final String name) {

	}

	/**
	 * Decodes all DomUI annotations.
	 * @param an
	 */
	protected void decodeAnnotation(final Annotation an) {
		if(an instanceof MetaCombo) {
			MetaCombo c = (MetaCombo) an;
			if(c.dataSet() != UndefinedComboDataSet.class)
				setComboDataSet(c.dataSet());
			if(c.labelRenderer() != UndefinedLabelStringRenderer.class)
				setComboLabelRenderer(c.labelRenderer());
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				setComboNodeRenderer(c.nodeRenderer());
			if(c.optional() != ComboOptionalType.INHERITED)
				m_comboOptional = c.optional();
			if(c.properties() != null && c.properties().length > 0) {
				m_comboDisplayProperties = DisplayPropertyMetaModel.decode(c.properties());
			}
			setComponentTypeHint(Constants.COMPONENT_COMBO);
		} else if(an instanceof MetaLookup) {
			MetaLookup c = (MetaLookup) an;
			if(c.nodeRenderer() != UndefinedLabelStringRenderer.class)
				m_lookupFieldRenderer = c.nodeRenderer();
			if(c.properties().length != 0)
				m_lookupFieldDisplayProperties = DisplayPropertyMetaModel.decode(c.properties());
			setComponentTypeHint(Constants.COMPONENT_LOOKUP);
		} else if(an instanceof MetaObject) {
			MetaObject mo = (MetaObject) an;
			if(mo.defaultColumns().length > 0) {
				m_tableDisplayProperties = DisplayPropertyMetaModel.decode(mo.defaultColumns());
			}
			if(!mo.defaultSortColumn().equals(Constants.NONE))
				setDefaultSortProperty(mo.defaultSortColumn());
			setDefaultSortDirection(mo.defaultSortOrder());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Resource bundle data.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Locates the bundle for the specified locale. Returns null if no bundle is present. The
	 * bundles found are cached for the locale's name.
	 */
	private synchronized ResourceBundle findBundle(final Locale loc) {
		ResourceBundle b = m_textMap.get(loc);
		if(b == null) {
			String base = m_metaClass.getName();
			try {
				b = ResourceBundle.getBundle(base, loc);
			} catch(Exception x) {
				b = NONE;
			}
			m_textMap.put(loc, b);
		}
		return b == NONE ? null : b;
	}

	/**
	 * This tries to find a label text for the property depending on the locale. It tries to
	 * open a resource file with the same name as the class, at the same location. If that
	 * works it reads the properties from there. If it cannot be found it defaults to the
	 * actual property name.
	 *
	 * @param p
	 * @param loc
	 * @return
	 */
	String getPropertyLabel(final DefaultPropertyMetaModel p, final Locale loc) {
		ResourceBundle b = findBundle(loc);
		if(b == null)
			return p.getName();
		try {
			return b.getString(p.getName() + ".label");
		} catch(Exception x) {}
		return p.getName();
	}

	String getPropertyHint(final DefaultPropertyMetaModel p, final Locale loc) {
		ResourceBundle b = findBundle(loc);
		if(b == null)
			return null;
		try {
			return b.getString(p.getName() + ".hint");
		} catch(Exception x) {}
		return null;
	}

	ResourceBundle getBundle(final Locale loc) {
		return findBundle(loc);
	}

	/**
	 * This resolves a property path, starting at this class. If any part of the path does
	 * not exist this returns null.
	 * @see to.etc.domui.component.meta.ClassMetaModel#findProperty(java.lang.String)
	 */
	public synchronized PropertyMetaModel findProperty(final String name) {
		PropertyMetaModel pmm = m_propertyMap.get(name);
		if(pmm != null)
			return pmm;
		pmm = MetaManager.internalCalculateDottedPath(this, name);
		if(pmm != null)
			m_propertyMap.put(name, pmm); // Save resolved path's property info
		return pmm;
		//		return m_propertyMap.get(name);
	}

	public synchronized PropertyMetaModel findSimpleProperty(final String name) {
		return m_propertyMap.get(name);
	}

	public List<PropertyMetaModel> getProperties() {
		return new ArrayList<PropertyMetaModel>(m_propertyMap.values());
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

	public boolean isInitialized() {
		return m_initialized;
	}

	public void initialized() {
		m_initialized = true;
	}

	public ComboOptionalType getComboOptional() {
		return m_comboOptional;
	}

	public void setComboOptional(final ComboOptionalType comboOptional) {
		m_comboOptional = comboOptional;
	}

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_comboNodeRenderer;
	}

	public void setComboNodeRenderer(final Class< ? extends INodeContentRenderer< ? >> comboNodeRenderer) {
		m_comboNodeRenderer = comboNodeRenderer;
	}

	public void addSearchProperty(final SearchPropertyMetaModel sp) {
		if(m_searchProperties == Collections.EMPTY_LIST)
			m_searchProperties = new ArrayList<SearchPropertyMetaModel>();
		m_searchProperties.add(sp);
	}

	public List<SearchPropertyMetaModel> getSearchProperties() {
		List<SearchPropertyMetaModel> list = new ArrayList<SearchPropertyMetaModel>(m_searchProperties);
		Collections.sort(list, new Comparator<SearchPropertyMetaModel>() {
			public int compare(final SearchPropertyMetaModel o1, final SearchPropertyMetaModel o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		return list;
	}

	public Class< ? > getActualClass() {
		return m_metaClass;
	}

	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_tableDisplayProperties;
	}

	public void setTableDisplayProperties(final List<DisplayPropertyMetaModel> tableDisplayProperties) {
		m_tableDisplayProperties = tableDisplayProperties;
	}

	public boolean isPersistentClass() {
		return m_persistentClass;
	}

	public void setPersistentClass(final boolean persistentClass) {
		m_persistentClass = persistentClass;
	}

	public String getDefaultSortProperty() {
		return m_defaultSortProperty;
	}

	public void setDefaultSortProperty(final String defaultSortProperty) {
		m_defaultSortProperty = defaultSortProperty;
	}

	public SortableType getDefaultSortDirection() {
		return m_defaultSortDirection;
	}

	public void setDefaultSortDirection(final SortableType defaultSortDirection) {
		m_defaultSortDirection = defaultSortDirection;
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

	public PropertyMetaModel getPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(final PropertyMetaModel primaryKey) {
		m_primaryKey = primaryKey;
	}

	@Override
	public String toString() {
		return "ClassMetaModel[" + m_metaClass.getName() + "]";
	}

	public String getUserEntityName() {
		if(m_userName != null)
			return m_userName;
		try {
			return getBundle(NlsContext.getLocale()).getString("entity.name");
		} catch(Exception x) {}

		return null;
		//		return getActualClass().getName().substring(getActualClass().getName().lastIndexOf('.')+1);
	}

	public String getUserEntityNamePlural() {
		if(m_userNamePlural != null)
			return m_userNamePlural;
		try {
			return getBundle(NlsContext.getLocale()).getString("entity.pluralname");
		} catch(Exception x) {}
		return null;
		//		return getActualClass().getName().substring(getActualClass().getName().lastIndexOf('.')+1);
	}

	public void setUserEntityName(final String s) {
		m_userName = s;
	}

	public void setUserEntityNamePlural(final String s) {
		m_userNamePlural = s;
	}

	public Object[] getDomainValues() {
		return m_domainValues;
	}

	public void setDomainValues(final Object[] domainValues) {
		m_domainValues = domainValues;
	}

	/**
	 * Retrieves a label value for the specified domain value. This is obtained from the appropriate
	 * bundle for this class.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getDomainLabel(java.util.Locale, java.lang.Object)
	 */
	public String getDomainLabel(final Locale loc, final Object value) {
		ResourceBundle b = findBundle(loc);
		if(b == null)
			return null;
		if(value instanceof Enum< ? >) {
			try {
				return b.getString(((Enum< ? >) value).name() + ".label");
			} catch(Exception x) {}
		}
		if(value instanceof Boolean) {
			return NlsContext.getGlobalMessage(((Boolean) value).booleanValue() ? Msgs.UI_BOOL_TRUE : Msgs.UI_BOOL_FALSE);
		}
		return null;
	}
}
