package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.list2.*;

public class ListChangeAssign<E> extends ListChange<E> {
	@Nullable
	final private List<E> m_oldValue;

	@Nullable
	final private List<E> m_newValue;

	public ListChangeAssign(@Nullable List<E> oldValue, @Nullable List<E> newValue) {
		m_oldValue = oldValue;
		m_newValue = newValue;
	}

	@Nullable
	public List<E> getNewValue() {
		return m_newValue;
	}

	@Nullable
	public List<E> getOldValue() {
		return m_oldValue;
	}

	@Override
	public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitAssign(this);
	}
}
