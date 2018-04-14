package to.etc.domui.databinding.list;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.list2.IListChangeVisitor;

public class ListChangeAdd<E> extends ListChange<E> {
	private int m_index;

	private E m_value;

	public ListChangeAdd(int index, @NonNull E value) {
		m_index = index;
		m_value = value;
	}

	public int getIndex() {
		return m_index;
	}

	@NonNull
	public E getValue() {
		return m_value;
	}

	@Override
	public void visit(@NonNull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitAdd(this);
	}
}
