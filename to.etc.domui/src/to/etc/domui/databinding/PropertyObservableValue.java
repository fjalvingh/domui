package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * An observed property on some instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class PropertyObservableValue<C, T> extends ListenerList<T, ValueChangeEvent<T>, IValueChangeListener<T>> implements IObservableValue<T> {
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
		/*
		 * 20130425 jal
		 * The old implementation did a getvalue before the setvalue, and fired an event when the
		 * old and new values changed. This should apparently not be done: the property we're observing
		 * itself will fire an event when modified.
		 */
		m_property.setValue(m_instance, value);

		//		T old = null;
		//		try {
		//			old = m_property.getValue(m_instance);
		//		} catch(Exception x) {}
		//		m_property.setValue(m_instance, value);
		//		notifyIfChanged(old, value);
	}

	void notifyIfChanged(@Nullable T old, @Nullable T value) {
		if(MetaManager.areObjectsEqual(old, value))
			return;
		ValueDiff<T> vd = new ValueDiff<T>(old, value);
		fireEvent(new ValueChangeEvent<T>(this, vd));
	}
}
