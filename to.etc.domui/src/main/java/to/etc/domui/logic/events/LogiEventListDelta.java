package to.etc.domui.logic.events;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;

final public class LogiEventListDelta<T, P, I> extends LogiEventBase {
	@NonNull
	final private T m_instance;

	@NonNull
	final private PropertyMetaModel<P> m_property;

	@Nullable
	final private I m_value;

	final private int m_index;

	@NonNull
	final private ListDeltaType m_type;

	public LogiEventListDelta(@NonNull String path, @NonNull T instance, @NonNull PropertyMetaModel<P> property, int index, @Nullable I value, @NonNull ListDeltaType type) {
		super(path);
		m_instance = instance;
		m_property = property;
		m_index = index;
		m_value = value;
		m_type = type;
	}

	@NonNull
	public ListDeltaType getType() {
		return m_type;
	}

	@NonNull
	public T getInstance() {
		return m_instance;
	}

	@NonNull
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
	void dump(@NonNull Appendable a) throws Exception {
		a.append(getPath()).append(" List:").append(m_type.toString()).append(" @index " + m_index + " value=").append(toString(m_value)).append("\n");
	}
}
