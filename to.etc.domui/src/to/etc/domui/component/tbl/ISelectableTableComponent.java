package to.etc.domui.component.tbl;

import javax.annotation.*;

public interface ISelectableTableComponent<T> {
	boolean isMultiSelectionVisible();

	void setShowSelection(boolean on);

	@Nullable
	ISelectionModel<T> getSelectionModel();
}
