package to.etc.domui.component.input;

public final class ValueLabelPair<T> {
	private T m_value;

	private String m_label;

	public ValueLabelPair(T value, String label) {
		m_value = value;
		m_label = label;
	}

	public T getValue() {
		return m_value;
	}

	public String getLabel() {
		return m_label;
	}

	@Override
	public String toString() {
		return getLabel() + "=" + getValue();
	}
}