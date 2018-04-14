package to.etc.domui.databinding.list;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.list2.IListChangeVisitor;

/**
 * The base class for all list change event items.
 * @param <E>	The list's element type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 31, 2013
 */
abstract public class ListChange<E> {
	/**
	 * Visit a handler to handle this change.
	 * @param visitor
	 * @throws Exception
	 */
	abstract public void visit(@NonNull IListChangeVisitor<E> visitor) throws Exception;
}
