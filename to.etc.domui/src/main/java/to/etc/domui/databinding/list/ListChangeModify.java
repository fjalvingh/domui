package to.etc.domui.databinding.list;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.list2.IListChangeVisitor;

public class ListChangeModify<E> extends ListChange<E> {
	final private int m_index;

	@NonNull
	private final E m_oldValue;

	@NonNull
	private final E m_newValue;

	public ListChangeModify(int index, E oldValue, E newValue) {
		m_index = index;
		m_oldValue = oldValue;
		m_newValue = newValue;
	}

	public int getIndex() {
		return m_index;
	}

	@NonNull
	public E getOldValue() {
		return m_oldValue;
	}

	@NonNull
	public E getNewValue() {
		return m_newValue;
	}

	@Override
	public void visit(@NonNull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitModify(this);
	}
}
