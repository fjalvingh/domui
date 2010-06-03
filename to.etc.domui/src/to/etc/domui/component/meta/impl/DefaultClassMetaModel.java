package to.etc.domui.component.meta.impl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This is a DomUI class metamodel info record that only contains data. It can be constructed by
 * metamodel factories and filled in by calling the appropriate setters. When an instance of this
 * class has been returned by a factory then it is NOT ALLOWED TO CHANGE IT ANYMORE(!) to maintain
 * thread-safety.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2010
 */
public class DefaultClassMetaModel implements ClassMetaModel {
	/** The class this is a class metamodel <i>for</i> */
	private final Class< ? > m_metaClass;

	private String m_classNameOnly;

	/** Theclass' resource bundle. */
	private BundleRef m_classBundle;

	private boolean m_initialized;

	private final Map<String, PropertyMetaModel> m_propertyMap = new HashMap<String, PropertyMetaModel>();

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

	private String m_tableName;

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

	private List<SearchPropertyMetaModelImpl> m_searchProperties = Collections.EMPTY_LIST;

	private List<SearchPropertyMetaModelImpl> m_keyWordSearchProperties = Collections.EMPTY_LIST;

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

	private Object[] m_domainValues;

	public DefaultClassMetaModel(final Class< ? > metaClass) {
		m_metaClass = metaClass;
		m_classNameOnly = metaClass.getName().substring(metaClass.getName().lastIndexOf('.') + 1);
		m_classBundle = BundleRef.create(metaClass, m_classNameOnly);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Resource bundle data.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the class' resource bundle.
	 */
	public BundleRef getClassBundle() {
		return m_classBundle;
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
		String s = getClassBundle().findMessage(loc, p.getName() + ".label");
		return s == null ? p.getName() : s;
	}

	/**
	 * Return the "hint" for a property.
	 * @param p
	 * @param loc
	 * @return
	 */
	String getPropertyHint(final DefaultPropertyMetaModel p, final Locale loc) {
		String v = getClassBundle().findMessage(loc, p.getName() + ".hint");
		if(v == null)
			v = getClassBundle().findMessage(loc, p.getName() + ".help");
		return v;
	}

	/**
	 * Return a user-presentable entity name for this class. This defaults to the classname itself if unset.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getUserEntityName()
	 */
	public String getUserEntityName() {
		String s = getClassBundle().findMessage(NlsContext.getLocale(), "entity.name");
		return s == null ? getClassNameOnly() : s;
	}

	/**
	 * Returns a user-presentable entity name as a plural name.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getUserEntityNamePlural()
	 */
	public String getUserEntityNamePlural() {
		String s = getClassBundle().findMessage(NlsContext.getLocale(), "entity.pluralname");
		return s == null ? getClassNameOnly() : s;
	}

	/**
	 * This resolves a property path, starting at this class. If any part of the path does
	 * not exist this returns null.
	 * @see to.etc.domui.component.meta.ClassMetaModel#findProperty(java.lang.String)
	 */
	@Nullable
	public synchronized PropertyMetaModel findProperty(final String name) {
		PropertyMetaModel pmm = m_propertyMap.get(name);
		if(pmm != null)
			return pmm;
		pmm = MetaManager.internalCalculateDottedPath(this, name);
		if(pmm != null)
			m_propertyMap.put(name, pmm); // Save resolved path's property info
		return pmm;
	}

	public synchronized PropertyMetaModel findSimpleProperty(final String name) {
		return m_propertyMap.get(name);
	}

	public synchronized void addProperty(PropertyMetaModel pmm) {
		m_propertyMap.put(pmm.getName(), pmm);
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

		//-- Finalize: sort search properties.
		Collections.sort(m_searchProperties, new Comparator<SearchPropertyMetaModelImpl>() {
			public int compare(final SearchPropertyMetaModelImpl o1, final SearchPropertyMetaModelImpl o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
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

	public void addSearchProperty(final SearchPropertyMetaModelImpl sp) {
		if(m_searchProperties == Collections.EMPTY_LIST)
			m_searchProperties = new ArrayList<SearchPropertyMetaModelImpl>();
		m_searchProperties.add(sp);
	}

	public void addKeyWordSearchProperty(final SearchPropertyMetaModelImpl sp) {
		if(m_keyWordSearchProperties == Collections.EMPTY_LIST)
			m_keyWordSearchProperties = new ArrayList<SearchPropertyMetaModelImpl>();
		m_keyWordSearchProperties.add(sp);
	}

	/**
	 * Returns the SORTED list of search properties defined on this class.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getSearchProperties()
	 */
	public List<SearchPropertyMetaModelImpl> getSearchProperties() {
		return m_searchProperties;
	}

	/**
	 * Returns the list of key word search properties defined on this class (unsorted).
	 * @see to.etc.domui.component.meta.ClassMetaModel#getKeyWordSearchProperties()
	 */
	public List<SearchPropertyMetaModelImpl> getKeyWordSearchProperties() {
		return m_keyWordSearchProperties;
	}

	public Class< ? > getActualClass() {
		return m_metaClass;
	}

	public synchronized List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		if(m_tableDisplayProperties == null || m_tableDisplayProperties.size() == 0) {
			m_tableDisplayProperties = MetaManager.calculateObjectProperties(this);
		}
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

	public String getTableName() {
		return m_tableName;
	}

	public void setTableName(String tableName) {
		m_tableName = tableName;
	}

	@Override
	public String toString() {
		return "ClassMetaModel[" + m_metaClass.getName() + "]";
	}

	public Object[] getDomainValues() {
		return m_domainValues;
	}

	public void setDomainValues(final Object[] domainValues) {
		m_domainValues = domainValues;
	}

	public String getClassNameOnly() {
		return m_classNameOnly;
	}

	/**
	 * Retrieves a label value for the specified domain value. This is obtained from the appropriate
	 * bundle for *this* class. Usually you would use the same call on a property, not on the class,
	 * since the property based version allows you to have property-specific translations for values.
	 * The property based version delegates here when no property based version is found.
	 *
	 * @see to.etc.domui.component.meta.ClassMetaModel#getDomainLabel(java.util.Locale, java.lang.Object)
	 */
	public String getDomainLabel(final Locale loc, final Object value) {
		if(value instanceof Enum< ? >) {
			try {
				String s = getClassBundle().findMessage(loc, ((Enum<?>) value).name() + ".label");
				return s; // jal 20090806 Must return null; let caller decide what the default should be.
			} catch(Exception x) {}
		}
		if(value instanceof Boolean)
			return Msgs.BUNDLE.getString(((Boolean) value).booleanValue() ? Msgs.UI_BOOL_TRUE : Msgs.UI_BOOL_FALSE);

		throw new IllegalStateException("Invalid call for non-domain object.");
		//		return null;
	}
}
