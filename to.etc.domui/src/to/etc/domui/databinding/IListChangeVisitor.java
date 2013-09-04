package to.etc.domui.databinding;

import javax.annotation.*;

public interface IListChangeVisitor<T> {
	public void visitAdd(@Nonnull ListChangeAdd<T> add) throws Exception;

	public void visitDelete(@Nonnull ListChangeDelete<T> add) throws Exception;

	public void visitModify(@Nonnull ListChangeModify<T> add) throws Exception;
}
