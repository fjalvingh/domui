package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.list2.*;

/**
 * Registration that a list property was assigned a new list, meaning that all of the content of the model has changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 1, 2013
 */
public class ListChangeAssign<E> extends ListChange<E> {
	@Nullable
	final private List<E> m_oldValue;

	@Nullable
	final private List<E> m_newValue;

	public ListChangeAssign(@Nullable List<E> oldValue, @Nullable List<E> newValue) {
		m_oldValue = oldValue;
		m_newValue = newValue;
	}

	/**
	 * The new list that was assigned to the property.
	 * @return
	 */
	@Nullable
	public List<E> getNewValue() {
		return m_newValue;
	}

	/**
	 * The old list that was inside the property.
	 * @return
	 */
	@Nullable
	public List<E> getOldValue() {
		return m_oldValue;
	}

	/**
	 * Visitor pattern handler.
	 * @see to.etc.domui.databinding.list.ListChange#visit(to.etc.domui.databinding.list2.IListChangeVisitor)
	 */
	@Override
	public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception {
		visitor.visitAssign(this);
	}
}
