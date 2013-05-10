package to.etc.domui.logic.events;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

final public class LogiEventListDelta<T, P, I> extends LogiEventBase {
	@Nonnull
	final private T m_instance;

	@Nonnull
	final private PropertyMetaModel<P> m_property;

	@Nullable
	final private I m_value;

	final private int m_index;

	@Nonnull
	final private ListDeltaType m_type;

	public LogiEventListDelta(@Nonnull String path, @Nonnull T instance, @Nonnull PropertyMetaModel<P> property, int index, @Nullable I value, @Nonnull ListDeltaType type) {
		super(path);
		m_instance = instance;
		m_property = property;
		m_index = index;
		m_value = value;
		m_type = type;
	}

	@Nonnull
	public ListDeltaType getType() {
		return m_type;
	}

	@Nonnull
	public T getInstance() {
		return m_instance;
	}

	@Nonnull
	public PropertyMetaModel<P> getProperty() {
		return m_property;
	}

	@Nullable
	public I getValue() {
		return m_value;
	}

	public int getIndex() {
		return m_index;
	}

	@Override
	void dump(@Nonnull Appendable a) throws Exception {
		a.append(getPath()).append(" List:").append(m_type.toString()).append(" @index " + m_index + " value=").append(toString(m_value)).append("\n");
	}
}
