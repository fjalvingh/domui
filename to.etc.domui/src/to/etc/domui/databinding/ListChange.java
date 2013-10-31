package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * The base class for all list change event items.
 * @param <E>	The list's element type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 31, 2013
 */
abstract public class ListChange<E> {
	abstract public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception;
}
