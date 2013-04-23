package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

public class PropertyObservableValue<C, T> extends ListenerList<T, ValueChangeEvent<T>, IValueChangeListener<T>> implements IObservableValue<T, ValueChangeEvent<T>, IValueChangeListener<T>> {
	@Nonnull
	final private C m_instance;

	@Nonnull
	final private PropertyMetaModel<T> m_property;

	public PropertyObservableValue(@Nonnull C instance, @Nonnull PropertyMetaModel<T> property) {
		m_instance = instance;
		m_property = property;
	}

	@Override
	@Nonnull
	public Class<T> getValueType() {
		return m_property.getActualType();
	}

	@Override
	@Nullable
	public T getValue() throws Exception {
		return m_property.getValue(m_instance);
	}

	@Override
	public void setValue(@Nullable T value) throws Exception {
		m_property.setValue(m_instance, value);
	}
}