package to.etc.domui.component.meta.impl;

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * This describes a normalized expanded display property. This is the base version
 * used for all single properties. When a property refers to another class which must
 * be rendered as multiple columns then the derived class ExpandedDisplayPropertyList
 * is used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class ExpandedDisplayProperty implements PropertyMetaModel {
	private ClassMetaModel m_classModel;

	private PropertyMetaModel m_propertyMeta;

	//	private DisplayPropertyMetaModel	m_displayMeta;
	private IValueAccessor< ? > m_accessor;

	private Class< ? > m_actualType;

	private int m_displayLength;

	private Class< ? extends IConverter< ? >> m_converterClass;

	private IConverter< ? > m_bestConverter;

	private SortableType m_sortableType = SortableType.UNKNOWN;

	private String m_propertyName;

	private String m_renderHint;

	private String m_defaultLabel;

	protected ExpandedDisplayProperty(DisplayPropertyMetaModel displayMeta, PropertyMetaModel propertyMeta, IValueAccessor< ? > accessor) {
		//		m_displayMeta = displayMeta;
		m_propertyMeta = propertyMeta;
		m_accessor = accessor;
		if(propertyMeta != null) { // ORDER 1
			m_defaultLabel = m_propertyMeta.getDefaultLabel();
			m_classModel = m_propertyMeta.getClassModel();
			m_actualType = m_propertyMeta.getActualType();
			m_propertyName = m_propertyMeta.getName();
			m_converterClass = propertyMeta.getConverterClass();
			m_bestConverter = propertyMeta.getBestConverter();
			if(m_sortableType == SortableType.UNKNOWN)
				setSortable(propertyMeta.getSortable());
			if(m_displayLength <= 0) {
				m_displayLength = propertyMeta.getDisplayLength();
				if(m_displayLength <= 0)
					m_displayLength = propertyMeta.getLength();
			}
		}
		if(displayMeta != null) { // ORDER 2 (overrides propertyMeta)
			if(displayMeta.getConverterClass() != null)
				m_converterClass = displayMeta.getConverterClass();
			if(displayMeta.getBestConverter() != null)
				m_bestConverter = displayMeta.getBestConverter();
			if(displayMeta.getSortable() != SortableType.UNKNOWN)
				setSortable(displayMeta.getSortable());
			if(displayMeta.getDisplayLength() > 0)
				m_displayLength = displayMeta.getDisplayLength();

			m_renderHint = displayMeta.getRenderHint();

			String s = displayMeta.getLabel();
			if(s != null)
				m_defaultLabel = s;
		}
	}

	/**
	 * Get the display expansion for a single property. If the property refers to a compound
	 * this will return an {@link ExpandedDisplayPropertyList}.
	 * @param clz
	 * @param property
	 * @return
	 */
	static public ExpandedDisplayProperty expandProperty(ClassMetaModel cmm, String property) {
		PropertyMetaModel pmm = cmm.findProperty(property); // Get primary metadata
		if(pmm == null)
			throw new IllegalStateException("Unknown property '" + property + "' on classModel=" + cmm);
		return expandProperty(pmm);
	}

	/**
	 * Get the display expansion for a single property. If the property refers to a compound
	 * this will return an {@link ExpandedDisplayPropertyList}.
	 * @param pmm		The metamodel for the property to expand.
	 */
	static public ExpandedDisplayProperty expandProperty(PropertyMetaModel pmm) {
		if(pmm == null)
			throw new IllegalArgumentException("Null property???");
		Class< ? > rescl = pmm.getActualType();
		if(!MetaManager.isSimpleClass(rescl)) {
			ClassMetaModel cmm = MetaManager.findClassMeta(rescl); // Find the property's type metadata (the meta for the class pointed to).

			if(pmm.getTableDisplayProperties().size() > 0 || cmm.getTableDisplayProperties().size() > 0) {
				/*
				 * The type pointed to, OR the property itself, has a set-of-columns to use to display the thingy.
				 */
				return expandCompoundProperty(pmm, cmm);
			}
		}

		//-- This is a single property (indivisable). Return it's info.
		return new ExpandedDisplayProperty(null, pmm, pmm.getAccessor());
	}

	static public List<ExpandedDisplayProperty> expandProperties(Class< ? > clz, String[] properties) {
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		return expandProperties(cmm, properties);
	}

	static public List<ExpandedDisplayProperty> expandProperties(ClassMetaModel cmm, String[] properties) {
		List<ExpandedDisplayProperty> res = new ArrayList<ExpandedDisplayProperty>(properties.length);
		for(String p : properties)
			res.add(expandProperty(cmm, p));
		return res;
	}

	/**
	 * Expands a compound property. If the originating property has a list-of-display-properties
	 * we expand these. The formal expansion strategy is:
	 * <ul>
	 * 	<li>If the core property has a </li>
	 * </ul>
	 *
	 * @param pmm
	 * @param cmm
	 * @return
	 */
	static private ExpandedDisplayProperty expandCompoundProperty(PropertyMetaModel pmm, ClassMetaModel cmm) {
		List<DisplayPropertyMetaModel> dpl = pmm.getTableDisplayProperties(); // Property itself has definition?
		if(dpl.size() == 0) {
			//-- No. Has class-referred-to a default?
			dpl = cmm.getTableDisplayProperties();
			if(dpl.size() == 0) {
				//-- Don't know how to display this. Just use a generic thingy causing a toString().
				return new ExpandedDisplayProperty(null, pmm, pmm.getAccessor());
			}
		}

		//-- We have a display list! Run the display list expander, using the property accessor as the base accessor.
		List<ExpandedDisplayProperty> list = expandDisplayProperties(dpl, cmm, pmm.getAccessor());
		return new ExpandedDisplayPropertyList(null, pmm, pmm.getAccessor(), list);
	}

	/**
	 * Enter with a list of display thingies; returns the fully-expanded list of thingeridoos.
	 * @param dpl
	 * @param cmm
	 * @param rootAccessor
	 * @return
	 */
	static public List<ExpandedDisplayProperty> expandDisplayProperties(List<DisplayPropertyMetaModel> dpl, ClassMetaModel cmm, IValueAccessor< ? > rootAccessor) {
		if(rootAccessor == null)
			rootAccessor = new IdentityAccessor<Object>();
		List<ExpandedDisplayProperty> res = new ArrayList<ExpandedDisplayProperty>(dpl.size());
		List<DisplayPropertyMetaModel> joinList = null;
		for(DisplayPropertyMetaModel dpm : dpl) {
			if(dpm.getJoin() != null) {
				if(joinList == null)
					joinList = new ArrayList<DisplayPropertyMetaModel>();
				joinList.add(dpm);
				continue;
			}

			//-- If we have a joinlist left handle that before handling the new property,
			if(joinList != null) {
				joinList.add(dpm);
				res.add(createJoinedProperty(cmm, joinList, rootAccessor));
				joinList = null;
				continue;
			}

			//-- Handle this (normal) property. Is this a DOTTED one?
			PropertyMetaModel pmm = cmm.findProperty(dpm.getName());
			if(pmm == null)
				throw new IllegalStateException("Unknown property " + dpm.getName() + " in " + cmm);
			Class< ? > clz = pmm.getActualType();
			List<DisplayPropertyMetaModel> subdpl = pmm.getTableDisplayProperties(); // Has defined sub-properties?
			ClassMetaModel pcmm = findCompoundClassModel(clz);
			if(subdpl.size() == 0 && pcmm != null) {
				//-- Has target-class a list of properties?
				subdpl = pcmm.getTableDisplayProperties();
			}

			//-- FIXME Handle embedded
			if(subdpl.size() != 0) {
				/*
				 * The property here is a COMPOUND property. Explicitly create a subthing for it,
				 */
				List<ExpandedDisplayProperty> xlist = expandDisplayProperties(subdpl, pcmm, new SubAccessor<Object, Object>((IValueAccessor<Object>) rootAccessor, (IValueAccessor<Object>) pmm
					.getAccessor()));
				res.add(new ExpandedDisplayPropertyList(null, pmm, pmm.getAccessor(), xlist));
				continue;
			}

			//-- Not a compound type-> add normal expansion (one column)
			ExpandedDisplayProperty xdp = new ExpandedDisplayProperty(dpm, pmm, new SubAccessor<Object, Object>((IValueAccessor<Object>) rootAccessor, (IValueAccessor<Object>) pmm.getAccessor()));
			res.add(xdp);
		}

		//-- If we have a joinlist left handle that before exiting.
		if(joinList != null) {
			res.add(createJoinedProperty(cmm, joinList, rootAccessor));
		}
		return res;
	}

	static private ClassMetaModel findCompoundClassModel(Class< ? > clz) {
		if(MetaManager.isSimpleClass(clz)) // Simple classes have no model
			return null;
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		return cmm;
		//		return cmm.getTableDisplayProperties().size() == 0 ? null : cmm;
	}

	/**
	 * This creates a joined property: a list of properties joined together in a string, separated by the join
	 * string.
	 *
	 * @param dpl
	 * @param accessor
	 * @return
	 */
	static private ExpandedDisplayProperty createJoinedProperty(ClassMetaModel cmm, List<DisplayPropertyMetaModel> dpl, IValueAccessor< ? > accessor) {
		//-- Create a paired list of PropertyMetaModel thingies
		List<PropertyMetaModel> plist = new ArrayList<PropertyMetaModel>(dpl.size());
		for(DisplayPropertyMetaModel dm : dpl) {
			PropertyMetaModel pm = cmm.findProperty(dm.getName());
			if(pm == null)
				throw new IllegalStateException("Display property " + dm.getName() + " not found on " + cmm);
			plist.add(pm);
		}
		return new JoinedDisplayProperty(dpl, plist, accessor);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing info on the expanded property.			*/
	/*--------------------------------------------------------------*/

	public IValueAccessor< ? > getAccessor() {
		return m_accessor;
	}

	public Class< ? > getActualType() {
		return m_actualType;
	}

	/**
	 * Returns null always; this seems reasonable for a type like this.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getGenericActualType()
	 */
	public Type getGenericActualType() {
		return null;
	}

	public String getDefaultLabel() {
		return m_defaultLabel;
	}

	public Class< ? extends IConverter< ? >> getConverterClass() {
		return m_converterClass;
	}

	public IConverter< ? > getBestConverter() {
		return m_bestConverter;
	}

	public void setBestConverter(IConverter< ? > bestConverter) {
		m_bestConverter = bestConverter;
	}

	public int getDisplayLength() {
		return m_displayLength;
	}

	public void setActualType(Class< ? > actualType) {
		m_actualType = actualType;
	}

	public void setDisplayLength(int displayLength) {
		m_displayLength = displayLength;
	}

	public SortableType getSortable() {
		if(m_sortableType == null)
			throw new IllegalStateException("?? Sortable may never be null??");
		return m_sortableType;
	}

	public void setSortable(SortableType sortableType) {
		if(sortableType == null)
			throw new IllegalStateException("?? Sortable may never be null??");
		m_sortableType = sortableType;
	}

	public String getName() {
		return m_propertyName;
	}

	public void setName(String propertyName) {
		m_propertyName = propertyName;
	}

	public String getPresentationString(Object root) throws Exception {
		Object colval = getAccessor().getValue(root);
		String s;
		if(colval == null)
			s = "";
		else {
			if(getConverterClass() != null)
				s = ConverterRegistry.convertValueToString((Class<IConverter<Object>>) getConverterClass(), colval);
			else
				s = colval.toString();
		}
		return s;
	}

	static public List<ExpandedDisplayProperty> flatten(List<ExpandedDisplayProperty> in) {
		List<ExpandedDisplayProperty> res = new ArrayList<ExpandedDisplayProperty>(in.size() + 10);
		flatten(res, in);
		return res;
	}

	static private void flatten(List<ExpandedDisplayProperty> res, List<ExpandedDisplayProperty> in) {
		for(ExpandedDisplayProperty xd : in)
			flatten(res, xd);
	}

	static public void flatten(List<ExpandedDisplayProperty> res, ExpandedDisplayProperty xd) {
		if(xd instanceof ExpandedDisplayPropertyList)
			flatten(res, ((ExpandedDisplayPropertyList) xd).getChildren());
		else
			res.add(xd);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	PropertyMetaModel proxy.							*/
	/*--------------------------------------------------------------*/
	/**
	 * This returns the ClassMetaModel for the ROOT of this property(!).
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getClassModel()
	 */
	public ClassMetaModel getClassModel() {
		return m_classModel;
	}

	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_propertyMeta != null ? m_propertyMeta.getComboDataSet() : null;
	}

	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComboDisplayProperties();
	}

	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComboLabelRenderer();
	}

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComboNodeRenderer();
	}

	public String getComponentTypeHint() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComponentTypeHint();
	}

	//	public Class< ? extends IConverter> getConverterClass() {
	//		return m_converterClass;
	//	}

	public String getDefaultHint() {
		return m_propertyMeta == null ? null : m_propertyMeta.getDefaultHint();
	}

	public String getDomainValueLabel(Locale loc, Object val) {
		return m_propertyMeta == null ? null : m_propertyMeta.getDomainValueLabel(loc, val);
	}

	public Object[] getDomainValues() {
		return m_propertyMeta == null ? null : m_propertyMeta.getDomainValues();
	}

	public String[][] getEditRoles() {
		return m_propertyMeta == null ? null : m_propertyMeta.getEditRoles();
	}

	public int getLength() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getLength();
	}

	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_propertyMeta == null ? null : m_propertyMeta.getLookupFieldDisplayProperties();
	}

	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getLookupFieldRenderer();
	}

	public int getPrecision() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getPrecision();
	}

	public YesNoType getReadOnly() {
		return m_propertyMeta == null ? null : m_propertyMeta.getReadOnly();
	}

	public PropertyRelationType getRelationType() {
		return m_propertyMeta == null ? null : m_propertyMeta.getRelationType();
	}

	public int getScale() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getScale();
	}

	//	public SortableType getSortable() {
	//		return m_propertyMeta == null ? null : m_propertyMeta.getS;
	//	}

	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_propertyMeta == null ? null : m_propertyMeta.getTableDisplayProperties();
	}

	public TemporalPresentationType getTemporal() {
		return m_propertyMeta == null ? null : m_propertyMeta.getTemporal();
	}

	public NumericPresentation getNumericPresentation() {
		return m_propertyMeta == null ? NumericPresentation.UNKNOWN : m_propertyMeta.getNumericPresentation();
	}

	public PropertyMetaValidator[] getValidators() {
		return m_propertyMeta == null ? null : m_propertyMeta.getValidators();
	}

	public String[][] getViewRoles() {
		return m_propertyMeta == null ? null : m_propertyMeta.getViewRoles();
	}

	public boolean isPrimaryKey() {
		return m_propertyMeta == null ? false : m_propertyMeta.isPrimaryKey();
	}

	public boolean isRequired() {
		return m_propertyMeta == null ? false : m_propertyMeta.isRequired();
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(String renderHint) {
		m_renderHint = renderHint;
	}

}
