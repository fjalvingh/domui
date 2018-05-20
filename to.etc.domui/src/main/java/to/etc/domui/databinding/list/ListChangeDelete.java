package to.etc.domui.databinding.list;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.list2.IListChangeVisitor;

public class ListChangeDelete<E> extends ListChange<E> {
	final private int m_index;

	@NonNull
	final private E m_value;

	public ListChangeDelete(int index, @NonNull E value) {
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
		visitor.visitDelete(this);
	}
}
