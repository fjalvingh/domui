package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * This is a proxy for an existing PropertyMetaModel for path-based properties. This overrides
 * the Accessor and replaces it with an accessor which walks the path to the target property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 29, 2008
 */
public class PathPropertyMetaModel<T> implements PropertyMetaModel, IValueAccessor<T> {
	private PropertyMetaModel m_original;

	private PropertyMetaModel[] m_accessPath;

	public PathPropertyMetaModel(PropertyMetaModel[] accessPath) {
		m_accessPath = accessPath;
		m_original = accessPath[accessPath.length - 1];
	}

	/**
	 * Calculate the value to get. If any path component is null this returns null, it does not
	 * throw exceptions.
	 *
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	public T getValue(Object in) throws Exception {
		Object cv = in;
		for(PropertyMetaModel pmm : m_accessPath) {
			cv = pmm.getAccessor().getValue(cv);
			if(cv == null)
				return null;
		}
		return (T) cv;
	}

	public void setValue(Object target, T value) throws Exception {
		Object cv = target;
		for(PropertyMetaModel pmm : m_accessPath) {
			if(pmm == m_original) { // Reached last segment?
				//-- Actually set a value now
				((IValueAccessor<T>) pmm.getAccessor()).setValue(cv, value);
				return;
			}

			cv = pmm.getAccessor().getValue(cv);
			if(cv == null)
				throw new IllegalStateException("The property '" + pmm.getName() + " in classModel=" + pmm.getClassModel() + " is null - cannot set a value!!");
		}
	}

	/**
	 * Create a compound accessor.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAccessor()
	 */
	public IValueAccessor< ? > getAccessor() {
		return this;
	}

	public Class< ? > getActualType() {
		return m_original.getActualType();
	}

	public ClassMetaModel getClassModel() {
		return m_original.getClassModel();
	}

	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_original.getComboDataSet();
	}

	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_original.getComboDisplayProperties();
	}

	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_original.getComboLabelRenderer();
	}

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_original.getComboNodeRenderer();
	}

	public String getComponentTypeHint() {
		return m_original.getComponentTypeHint();
	}

	public Class<? extends IConverter<?>> getConverterClass() {
		return m_original.getConverterClass();
	}

	public IConverter<?> getBestConverter() {
		return m_original.getBestConverter();
	}

	public String getDefaultHint() {
		return m_original.getDefaultHint();
	}

	public String getDefaultLabel() {
		return m_original.getDefaultLabel();
	}

	public int getDisplayLength() {
		return m_original.getDisplayLength();
	}

	public String getDomainValueLabel(Locale loc, Object val) {
		return m_original.getDomainValueLabel(loc, val);
	}

	public Object[] getDomainValues() {
		return m_original.getDomainValues();
	}

	public String[][] getEditRoles() {
		return m_original.getEditRoles();
	}

	public int getLength() {
		return m_original.getLength();
	}

	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_original.getLookupFieldDisplayProperties();
	}

	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_original.getLookupFieldRenderer();
	}

	public String getName() {
		StringBuilder name = new StringBuilder();
		for (PropertyMetaModel pmm: m_accessPath) {
			name.append(pmm.getName());
			if (!pmm.equals(m_original)) {
				name.append(".");
			}
		}

		return name.toString();
		//return m_original.getName();
	}

	public int getPrecision() {
		return m_original.getPrecision();
	}

	public YesNoType getReadOnly() {
		return m_original.getReadOnly();
	}

	public PropertyRelationType getRelationType() {
		return m_original.getRelationType();
	}

	public int getScale() {
		return m_original.getScale();
	}

	public SortableType getSortable() {
		return m_original.getSortable();
	}

	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_original.getTableDisplayProperties();
	}

	public TemporalPresentationType getTemporal() {
		return m_original.getTemporal();
	}

	public NumericPresentation getNumericPresentation() {
		return m_original.getNumericPresentation();
	}

	public PropertyMetaValidator[] getValidators() {
		return m_original.getValidators();
	}

	public String[][] getViewRoles() {
		return m_original.getViewRoles();
	}

	public boolean isPrimaryKey() {
		return m_original.isPrimaryKey();
	}

	public boolean isRequired() {
		return m_original.isRequired();
	}
}
