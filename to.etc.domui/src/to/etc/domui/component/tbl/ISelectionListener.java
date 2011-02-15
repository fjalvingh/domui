package to.etc.domui.component.tbl;

import javax.annotation.*;

public interface ISelectionListener<T> {
	void selectionChanged(@Nonnull T row, boolean on);
}
