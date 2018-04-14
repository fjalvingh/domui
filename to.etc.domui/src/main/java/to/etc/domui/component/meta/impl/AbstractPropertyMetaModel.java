package to.etc.domui.component.meta.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.controlfactory.PropertyControlFactory;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.converter.IConverter;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.ILabelStringRenderer;
import to.etc.domui.util.IRenderInto;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4/21/16.
 */
abstract public class AbstractPropertyMetaModel<T> implements PropertyMetaModel<T> {
	final private ClassMetaModel m_classMetaModel;

	final private Class<T> m_actualType;

	final private String m_name;

	protected AbstractPropertyMetaModel(ClassMetaModel classMetaModel, Class<T> actualType, String name) {
		m_classMetaModel = classMetaModel;
		m_actualType = actualType;
		m_name = name;
	}

	@NonNull
	@Override
	public ClassMetaModel getClassModel() {
		return m_classMetaModel;
	}

	@Nullable
	@Override
	public ClassMetaModel getValueModel() {
		return MetaManager.findClassMeta(getActualType());
	}

	@NonNull
	@Override
	public Class<T> getActualType() {
		return m_actualType;
	}

	@Nullable
	@Override
	public Type getGenericActualType() {
		return null;
	}

	@NonNull
	@Override
	public String getDefaultLabel() {
		throw new IllegalStateException("Not implemented");
	}

	@Nullable
	@Override
	public String getDefaultHint() {
		return null;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public int getPrecision() {
		return 0;
	}

	@Override
	public int getScale() {
		return 0;
	}

	@Override
	public int getDisplayLength() {
		return 0;
	}

	@NonNull
	@Override
	public String getName() {
		return m_name;
	}

	@NonNull
	@Override
	public SortableType getSortable() {
		return SortableType.UNKNOWN;
	}

	@Nullable
	@Override
	public IConverter<T> getConverter() {
		return null;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public boolean isPrimaryKey() {
		return false;
	}

	@NonNull
	@Override
	public PropertyRelationType getRelationType() {
		return PropertyRelationType.NONE;
	}

	@Nullable
	@Override
	public Object[] getDomainValues() {
		return new Object[0];
	}

	@Nullable
	@Override
	public String getDomainValueLabel(Locale loc, Object val) {
		return null;
	}

	@NonNull
	@Override
	public TemporalPresentationType getTemporal() {
		return TemporalPresentationType.UNKNOWN;
	}

	@NonNull
	@Override
	public NumericPresentation getNumericPresentation() {
		return NumericPresentation.UNKNOWN;
	}

	@Nullable
	@Override
	public Class<? extends IComboDataSet<?>> getComboDataSet() {
		return null;
	}

	@Nullable
	@Override
	public Class<? extends ILabelStringRenderer<?>> getComboLabelRenderer() {
		return null;
	}

	@Nullable
	@Override
	public Class<? extends IRenderInto<T>> getComboNodeRenderer() {
		return null;
	}

	@NonNull
	@Override
	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public YesNoType getReadOnly() {
		return YesNoType.UNKNOWN;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Nullable
	@Override
	public String getComponentTypeHint() {
		return null;
	}

	@Nullable
	@Override
	public Class<? extends IRenderInto<T>> getLookupSelectedRenderer() {
		return null;
	}

	@NonNull
	@Override
	public List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public List<DisplayPropertyMetaModel> getLookupTableProperties() {
		return Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public List<SearchPropertyMetaModel> getLookupFieldSearchProperties() {
		return Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties() {
		return Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public PropertyMetaValidator[] getValidators() {
		return new PropertyMetaValidator[0];
	}

	@Nullable
	@Override
	public String getRegexpValidator() {
		return null;
	}

	@Nullable
	@Override
	public String getRegexpUserString() {
		return null;
	}

	@Nullable
	@Override
	public PropertyControlFactory getControlFactory() {
		return null;
	}

	@Nullable
	@Override
	public <A> A getAnnotation(@NonNull Class<A> annclass) {
		return null;
	}

	@NonNull
	@Override
	public List<Object> getAnnotations() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String[] getColumnNames() {
		return new String[0];
	}

	@Nullable
	@Override
	public IQueryManipulator<T> getQueryManipulator() {
		return null;
	}

	@NonNull
	@Override
	public YesNoType getNowrap() {
		return YesNoType.UNKNOWN;
	}

	@Override
	public void setValue(@NonNull Object target, @Nullable T value) throws Exception {
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Nullable
	@Override
	public T getValue(@NonNull Object in) throws Exception {
		return null;
	}
}
