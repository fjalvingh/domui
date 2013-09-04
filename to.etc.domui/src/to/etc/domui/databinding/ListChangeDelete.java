package to.etc.domui.databinding;

import javax.annotation.*;

public class ListChangeDelete<T> extends ListChange<T> {
	final private int m_index;

	@Nonnull
	final private T m_value;

	public ListChangeDelete(int index, @Nonnull T value) {
		m_index = index;
		m_value = value;
	}

	public int getIndex() {
		return m_index;
	}

	@Nonnull
	public T getValue() {
		return m_value;
	}
}
