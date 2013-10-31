package to.etc.domui.databinding;

import javax.annotation.*;

public class ListChangeAdd<E> extends ListChange<E> {
	private int m_index;

	private E m_value;

	public ListChangeAdd(int index, @Nonnull E value) {
		m_index = index;
		m_value = value;
	}

	public int getIndex() {
		return m_index;
	}

	@Nonnull
	public E getValue() {
		return m_value;
	}

	@Override
	public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitAdd(this);
	}
}
