package to.etc.domui.databinding;

import javax.annotation.*;

abstract public class ListChange<T> {
	abstract public void visit(@Nonnull IListChangeVisitor<T> visitor) throws Exception;
}
