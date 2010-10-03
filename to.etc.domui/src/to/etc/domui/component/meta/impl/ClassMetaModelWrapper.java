package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

public class ClassMetaModelWrapper implements ClassMetaModel {
	private ClassMetaModel m_parent;

	protected ClassMetaModelWrapper(ClassMetaModel parent) {
		m_parent = parent;
	}

	public ClassMetaModel getWrappedModel() {
		return m_parent;
	}

	/**
	 * WATCH OUT: Should only be used when initializing outside the constructor; should not change after this
	 * has been passed to user code.
	 * @param parent
	 */
	public void setWrappedModel(ClassMetaModel parent) {
		m_parent = parent;
	}

	@Override
	public PropertyMetaModel findProperty(String name) {
		return m_parent.findProperty(name);
	}

	@Override
	public PropertyMetaModel findSimpleProperty(String name) {
		return m_parent.findSimpleProperty(name);
	}

	@Override
	public Class< ? > getActualClass() {
		return m_parent.getActualClass();
	}

	@Override
	public BundleRef getClassBundle() {
		return m_parent.getClassBundle();
	}

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_parent.getComboDataSet();
	}

	@Override
	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_parent.getComboDisplayProperties();
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_parent.getComboLabelRenderer();
	}

	@Override
	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_parent.getComboNodeRenderer();
	}

	@Override
	public String getComponentTypeHint() {
		return m_parent.getComponentTypeHint();
	}

	@Override
	public SortableType getDefaultSortDirection() {
		return m_parent.getDefaultSortDirection();
	}

	@Override
	public String getDefaultSortProperty() {
		return m_parent.getDefaultSortProperty();
	}

	@Override
	public String getDomainLabel(Locale loc, Object value) {
		return m_parent.getDomainLabel(loc, value);
	}

	@Override
	public Object[] getDomainValues() {
		return m_parent.getDomainValues();
	}

	@Override
	public List<SearchPropertyMetaModel> getKeyWordSearchProperties() {
		return m_parent.getKeyWordSearchProperties();
	}

	@Override
	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_parent.getLookupFieldDisplayProperties();
	}

	@Override
	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_parent.getLookupFieldRenderer();
	}

	@Override
	public PropertyMetaModel getPrimaryKey() {
		return m_parent.getPrimaryKey();
	}

	@Override
	public List<PropertyMetaModel> getProperties() {
		return m_parent.getProperties();
	}

	@Override
	public List<SearchPropertyMetaModel> getSearchProperties() {
		return m_parent.getSearchProperties();
	}

	@Override
	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_parent.getTableDisplayProperties();
	}

	@Override
	public String getTableName() {
		return m_parent.getTableName();
	}

	@Override
	public String getUserEntityName() {
		return m_parent.getUserEntityName();
	}

	@Override
	public String getUserEntityNamePlural() {
		return m_parent.getUserEntityNamePlural();
	}

	@Override
	public boolean isPersistentClass() {
		return m_parent.isPersistentClass();
	}

	@Override
	public QCriteria< ? > createCriteria() throws Exception {
		return m_parent.createCriteria();
	}
}
