/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.meta.impl;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.init.MetaInitializer;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import java.util.*;

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

	private ICriteriaTableDef< ? > m_metaTableDef;

	private String m_classNameOnly;

	/** Theclass' resource bundle. */
	@Nonnull
	final private BundleRef m_classBundle;

	/** An immutable list of all properties of this class. */
	private List<PropertyMetaModel< ? >> m_rootProperties;

	/** All undotted properties, set at initialization time, */
	@Nonnull
	private Map<String, PropertyMetaModel< ? >> m_simplePropertyMap = Collections.EMPTY_MAP;

	@Nonnull
	final private Map<String, PropertyMetaModel< ? >> m_dottedPropertyMap = new HashMap<>();

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

	@Nullable
	private IQueryManipulator< ? > m_queryManipulator;

	@Nullable
	private String m_comboSortProperty;

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	private Class< ? extends ILabelStringRenderer< ? >> m_comboLabelRenderer;

	private Class< ? extends IRenderInto< ? >> m_comboNodeRenderer;

//	private ComboOptionalType m_comboOptional;

	@Nonnull
	private List<DisplayPropertyMetaModel> m_comboDisplayProperties = Collections.EMPTY_LIST;

	@Nonnull
	private List<DisplayPropertyMetaModel> m_tableDisplayProperties = Collections.EMPTY_LIST;

	@Nonnull
	private List<SearchPropertyMetaModel> m_searchProperties = Collections.EMPTY_LIST;

	@Nonnull
	private List<SearchPropertyMetaModel> m_keyWordSearchProperties = Collections.EMPTY_LIST;

	/**
	 * Default renderer which renders a lookup field's "field" contents; this is a table which must be filled with
	 * data pertaining to the looked-up item as a single element on the "edit" screen.
	 */
	private Class< ? extends IRenderInto< ? >> m_lookupFieldRenderer;

	/**
	 * The default properties to show in a {@link LookupInput} field's instance display.
	 */
	@Nonnull
	private List<DisplayPropertyMetaModel> m_lookupFieldDisplayProperties = Collections.EMPTY_LIST;

	private String m_defaultSortProperty;

	@Nullable
	private SortableType m_defaultSortDirection;

	private PropertyMetaModel< ? > m_primaryKey;

	private Object[] m_domainValues;

	public DefaultClassMetaModel(final Class< ? > metaClass) {
		m_metaClass = metaClass;
		m_classNameOnly = metaClass.getName().substring(metaClass.getName().lastIndexOf('.') + 1);
		m_classBundle = BundleRef.create(metaClass, m_classNameOnly);
	}

	@GuardedBy("MetaManager.class")
	public void setClassProperties(List<PropertyMetaModel< ? >> reslist) {
		m_rootProperties = Collections.unmodifiableList(reslist);
		Map<String, PropertyMetaModel<?>> propMap = new HashMap<>();		// Set all undotted properties
		for(PropertyMetaModel< ? > pmm : reslist) {
			propMap.put(pmm.getName(), pmm);
		}
		m_simplePropertyMap = Collections.unmodifiableMap(propMap);			// Save,
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Resource bundle data.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the class' resource bundle.
	 */
	@Override
	@Nonnull
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
	@Nonnull
	String getPropertyLabel(final DefaultPropertyMetaModel< ? > p, final Locale loc) {
		String s = getClassBundle().findMessage(loc, p.getName() + ".label");
		return s == null ? p.getName() : s;
	}

	/**
	 * Return the "hint" for a property.
	 * @param p
	 * @param loc
	 * @return
	 */
	@Nullable
	String getPropertyHint(final DefaultPropertyMetaModel< ? > p, final Locale loc) {
		String v = getClassBundle().findMessage(loc, p.getName() + ".hint");
		if(v == null)
			v = getClassBundle().findMessage(loc, p.getName() + ".help");
		return v;
	}

	/**
	 * Return a user-presentable entity name for this class. Returns null if not set.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getUserEntityName()
	 */
	@Override
	@Nullable
	public String getUserEntityName() {
		return getClassBundle().findMessage(NlsContext.getLocale(), "entity.name");
	}

	/**
	 * Returns a user-presentable entity name as a plural name.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getUserEntityNamePlural()
	 */
	@Override
	@Nullable
	public String getUserEntityNamePlural() {
		return getClassBundle().findMessage(NlsContext.getLocale(), "entity.pluralname");
	}

	/**
	 * This resolves a property path, starting at this class. If any part of the path does
	 * not exist this returns null.
	 * @see to.etc.domui.component.meta.ClassMetaModel#findProperty(java.lang.String)
	 */
	@Override
	@Nullable
	public PropertyMetaModel< ? > findProperty(@Nonnull final String name) {
		if(name.indexOf('.') == -1)						// No dot?
			return findSimpleProperty(name);			// Find it without lock

		/*
		 * We need to check the dotted map, and we need to prevent deadlocking the system when multiple classes are
		  * initializing.
		 */
		PropertyMetaModel< ? > pmm;
		synchronized(this) {
			pmm = m_dottedPropertyMap.get(name);
			if(pmm != null)
				return pmm;
		}

		//-- Create a compound property outside the lock; this prevents deadlock at the costs of running several copies at the same time.
		pmm = MetaInitializer.internalCalculateDottedPath(this, name);
		if(pmm != null) {
			/*
			 * Now resolve the possible multiple resolutions of the same dotted path, by checking if some other thread "stored first".
			 */
			synchronized(this) {
				PropertyMetaModel<?> racePmm = m_dottedPropertyMap.get(name);        // Was a path stored in the meanwhile?
				if(null != racePmm)
					return racePmm;                                                    // Yes-> the earlier thread won, use it's result
				m_dottedPropertyMap.put(name, pmm);                // We won ;)
			}
		}
		return pmm;
	}

	@Override
	@Nonnull
	public PropertyMetaModel< ? > getProperty(@Nonnull String name) {
		PropertyMetaModel< ? > pmm = findProperty(name);
		if(null == pmm)
			throw new IllegalStateException("The property '" + name + "' is not known in the meta model for " + this);
		return pmm;
	}

	/**
	 * Not synchronized because the simple map is initialized when the ClassMetaModel is created and after that it's immutable.
	 * @param name
	 * @return
	 */
	@Override
	@Nullable
	public PropertyMetaModel< ? > findSimpleProperty(@Nonnull final String name) {
		return m_simplePropertyMap.get(name);
	}

	@Override
	@Nonnull
	public List<PropertyMetaModel< ? >> getProperties() {
		return m_rootProperties;
	}

	@Override
	@Nullable
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_comboDataSet;
	}

	public void setComboDataSet(@Nullable final Class< ? extends IComboDataSet< ? >> comboDataSet) {
		m_comboDataSet = comboDataSet;
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_comboLabelRenderer;
	}

	public void setComboLabelRenderer(final Class< ? extends ILabelStringRenderer< ? >> comboLabelRenderer) {
		m_comboLabelRenderer = comboLabelRenderer;
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_comboDisplayProperties;
	}

	public void setComboDisplayProperties(final List<DisplayPropertyMetaModel> displayProperties) {
		m_comboDisplayProperties = displayProperties;
	}

	@Override
	public Class< ? extends IRenderInto< ? >> getComboNodeRenderer() {
		return m_comboNodeRenderer;
	}

	public void setComboNodeRenderer(final Class< ? extends IRenderInto< ? >> comboNodeRenderer) {
		m_comboNodeRenderer = comboNodeRenderer;
	}

	/**
	 * Returns the SORTED list of search properties defined on this class.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getSearchProperties()
	 */
	@Override
	@Nonnull
	public List<SearchPropertyMetaModel> getSearchProperties() {
		return m_searchProperties;
	}

	public void setSearchProperties(@Nonnull List<SearchPropertyMetaModel> searchProperties) {
		m_searchProperties = searchProperties.size() == 0 ? Collections.EMPTY_LIST : searchProperties;
	}

	/**
	 * Returns the sorted list of key word search properties defined on this class.
	 * @see to.etc.domui.component.meta.ClassMetaModel#getKeyWordSearchProperties()
	 */
	@Override
	@Nonnull
	public List<SearchPropertyMetaModel> getKeyWordSearchProperties() {
		return m_keyWordSearchProperties;
	}

	public void setKeyWordSearchProperties(@Nonnull List<SearchPropertyMetaModel> keyWordSearchProperties) {
		m_keyWordSearchProperties = keyWordSearchProperties.size() == 0 ? Collections.EMPTY_LIST : keyWordSearchProperties;
	}

	@Override
	public @Nonnull Class< ? > getActualClass() {
		return m_metaClass;
	}

	@Override
	public synchronized @Nonnull List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		if(m_tableDisplayProperties == null || m_tableDisplayProperties.size() == 0) {
			m_tableDisplayProperties = MetaManager.calculateObjectProperties(this);
		}
		return m_tableDisplayProperties;
	}

	public synchronized void setTableDisplayProperties(final List<DisplayPropertyMetaModel> tableDisplayProperties) {
		m_tableDisplayProperties = tableDisplayProperties;
	}

	@Override
	public boolean isPersistentClass() {
		return m_persistentClass;
	}

	public void setPersistentClass(final boolean persistentClass) {
		m_persistentClass = persistentClass;
	}

	@Override
	public String getDefaultSortProperty() {
		return m_defaultSortProperty;
	}

	public void setDefaultSortProperty(final String defaultSortProperty) {
		m_defaultSortProperty = defaultSortProperty;
	}

	@Override
	public @Nullable SortableType getDefaultSortDirection() {
		return m_defaultSortDirection;
	}

	public void setDefaultSortDirection(@Nullable final SortableType defaultSortDirection) {
		m_defaultSortDirection = defaultSortDirection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class< ? extends IRenderInto< ? >> getLookupSelectedRenderer() {
		return m_lookupFieldRenderer;
	}

	public void setLookupSelectedRenderer(final Class< ? extends IRenderInto< ? >> lookupFieldRenderer) {
		m_lookupFieldRenderer = lookupFieldRenderer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_lookupFieldDisplayProperties;
	}

	public void setLookupSelectedProperties(final List<DisplayPropertyMetaModel> lookupFieldDisplayProperties) {
		m_lookupFieldDisplayProperties = lookupFieldDisplayProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getComponentTypeHint() {
		return m_componentTypeHint;
	}

	public void setComponentTypeHint(final String componentTypeHint) {
		m_componentTypeHint = componentTypeHint;
	}

	@Override
	public PropertyMetaModel< ? > getPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(final PropertyMetaModel< ? > primaryKey) {
		m_primaryKey = primaryKey;
	}

	@Override
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

	@Override
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
	@Nullable
	@Override
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

	@Nullable
	public ICriteriaTableDef< ? > getMetaTableDef() {
		return m_metaTableDef;
	}

	public void setMetaTableDef(@Nullable ICriteriaTableDef< ? > metaTableDef) {
		m_metaTableDef = metaTableDef;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.component.meta.ClassMetaModel#createCriteria()
	 */
	@Override
	@Nonnull
	public QCriteria< ? > createCriteria() throws Exception {
		ICriteriaTableDef< ? > tdef = getMetaTableDef();
		if(tdef != null)
			return QCriteria.create(tdef);
		return QCriteria.create(getActualClass());
	}

	@Override
	public IQueryManipulator< ? > getQueryManipulator() {
		return m_queryManipulator;
	}

	public void setQueryManipulator(IQueryManipulator< ? > queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	@Override
	public String getComboSortProperty() {
		return m_comboSortProperty;
	}

	public void setComboSortProperty(String comboSortProperty) {
		m_comboSortProperty = comboSortProperty;
	}


}
