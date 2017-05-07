package to.etc.domui.databinding.list2;

import javax.annotation.*;

import to.etc.domui.databinding.list.*;

/**
 * Visitor that can be used to apply all changes registered inside a {@link ListChangeEvent}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 1, 2013
 */
public interface IListChangeVisitor<T> {
	public void visitAssign(@Nonnull ListChangeAssign<T> assign) throws Exception;

	public void visitAdd(@Nonnull ListChangeAdd<T> add) throws Exception;

	public void visitDelete(@Nonnull ListChangeDelete<T> add) throws Exception;

	public void visitModify(@Nonnull ListChangeModify<T> add) throws Exception;
}
