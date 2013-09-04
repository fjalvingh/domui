package to.etc.domui.databinding;

import javax.annotation.*;

public class ListChangeAdd<T> extends ListChange<T> {
	private int m_index;

	private T m_value;

	public ListChangeAdd(int index, @Nonnull T value) {
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

	@Override
	public void visit(@Nonnull IListChangeVisitor<T> visitor) throws Exception {
		visitor.visitAdd(this);
	}
}
