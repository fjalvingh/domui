package to.etc.domui.databinding;

import javax.annotation.*;

public class ListChangeModify<T> extends ListChange<T> {
	final private int m_index;

	@Nonnull
	private final T m_oldValue;

	@Nonnull
	private final T m_newValue;

	public ListChangeModify(int index, T oldValue, T newValue) {
		m_index = index;
		m_oldValue = oldValue;
		m_newValue = newValue;
	}

	public int getIndex() {
		return m_index;
	}

	@Nonnull
	public T getOldValue() {
		return m_oldValue;
	}

	@Nonnull
	public T getNewValue() {
		return m_newValue;
	}

	@Override
	public void visit(IListChangeVisitor<T> visitor) throws Exception {
		visitor.visitModify(this);
	}
}
