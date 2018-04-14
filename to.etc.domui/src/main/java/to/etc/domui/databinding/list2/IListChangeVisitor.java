package to.etc.domui.databinding.list2;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.list.ListChangeAdd;
import to.etc.domui.databinding.list.ListChangeAssign;
import to.etc.domui.databinding.list.ListChangeDelete;
import to.etc.domui.databinding.list.ListChangeModify;

/**
 * Visitor that can be used to apply all changes registered inside a {@link ListChangeEvent}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 1, 2013
 */
public interface IListChangeVisitor<T> {
	void visitAssign(@NonNull ListChangeAssign<T> assign) throws Exception;

	void visitAdd(@NonNull ListChangeAdd<T> add) throws Exception;

	void visitDelete(@NonNull ListChangeDelete<T> add) throws Exception;

	void visitModify(@NonNull ListChangeModify<T> add) throws Exception;
}
