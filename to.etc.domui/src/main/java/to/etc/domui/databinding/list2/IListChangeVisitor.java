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
	void visitAssign(@Nonnull ListChangeAssign<T> assign) throws Exception;

	void visitAdd(@Nonnull ListChangeAdd<T> add) throws Exception;

	void visitDelete(@Nonnull ListChangeDelete<T> add) throws Exception;

	void visitModify(@Nonnull ListChangeModify<T> add) throws Exception;
}
