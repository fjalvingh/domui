package to.etc.domui.databinding.list;

import javax.annotation.*;

import to.etc.domui.databinding.list2.*;

public class ListChangeModify<E> extends ListChange<E> {
	final private int m_index;

	@Nonnull
	private final E m_oldValue;

	@Nonnull
	private final E m_newValue;

	public ListChangeModify(int index, E oldValue, E newValue) {
		m_index = index;
		m_oldValue = oldValue;
		m_newValue = newValue;
	}

	public int getIndex() {
		return m_index;
	}

	@Nonnull
	public E getOldValue() {
		return m_oldValue;
	}

	@Nonnull
	public E getNewValue() {
		return m_newValue;
	}

	@Override
	public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitModify(this);
	}
}
