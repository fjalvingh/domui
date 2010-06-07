package to.etc.domui.component.meta.impl;

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

abstract public class PropertyMetaModelWrapper implements PropertyMetaModel {
	private PropertyMetaModel m_parent;

	protected PropertyMetaModelWrapper(PropertyMetaModel parent) {
		m_parent = parent;
	}

	public PropertyMetaModel getWrappedModel() {
		return m_parent;
	}

	/**
	 * WATCH OUT: Should only be used when initializing outside the constructor; should not change after this
	 * has been passed to user code.
	 * @param parent
	 */
	public void setWrappedModel(PropertyMetaModel parent) {
		m_parent = parent;
	}

	@Override
	public IValueAccessor< ? > getAccessor() {
		return m_parent.getAccessor();
	}

	@Override
	public Class< ? > getActualType() {
		return m_parent.getActualType();
	}

	@Override
	abstract public ClassMetaModel getClassModel();

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
	public ControlFactory getControlFactory() {
		return m_parent.getControlFactory();
	}

	@Override
	public IConverter< ? > getConverter() {
		return m_parent.getConverter();
	}

	@Override
	public String getDefaultHint() {
		return m_parent.getDefaultHint();
	}

	@Override
	public String getDefaultLabel() {
		return m_parent.getDefaultLabel();
	}

	@Override
	public int getDisplayLength() {
		return m_parent.getDisplayLength();
	}

	@Override
	public String getDomainValueLabel(Locale loc, Object val) {
		return m_parent.getDomainValueLabel(loc, val);
	}

	@Override
	public Object[] getDomainValues() {
		return m_parent.getDomainValues();
	}

	@Override
	public String[][] getEditRoles() {
		return m_parent.getEditRoles();
	}

	@Override
	public Type getGenericActualType() {
		return m_parent.getGenericActualType();
	}

	@Override
	public int getLength() {
		return m_parent.getLength();
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
	public String getName() {
		return m_parent.getName();
	}

	@Override
	public NumericPresentation getNumericPresentation() {
		return m_parent.getNumericPresentation();
	}

	@Override
	public int getPrecision() {
		return m_parent.getPrecision();
	}

	@Override
	public YesNoType getReadOnly() {
		return m_parent.getReadOnly();
	}

	@Override
	public String getRegexpUserString() {
		return m_parent.getRegexpUserString();
	}

	@Override
	public String getRegexpValidator() {
		return m_parent.getRegexpValidator();
	}

	@Override
	public PropertyRelationType getRelationType() {
		return m_parent.getRelationType();
	}

	@Override
	public int getScale() {
		return m_parent.getScale();
	}

	@Override
	public SortableType getSortable() {
		return m_parent.getSortable();
	}

	@Override
	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_parent.getTableDisplayProperties();
	}

	@Override
	public TemporalPresentationType getTemporal() {
		return m_parent.getTemporal();
	}

	@Override
	public PropertyMetaValidator[] getValidators() {
		return m_parent.getValidators();
	}

	@Override
	public String[][] getViewRoles() {
		return m_parent.getViewRoles();
	}

	@Override
	public boolean isPrimaryKey() {
		return m_parent.isPrimaryKey();
	}

	@Override
	public boolean isRequired() {
		return m_parent.isRequired();
	}

	@Override
	public boolean isTransient() {
		return m_parent.isTransient();
	}
}
