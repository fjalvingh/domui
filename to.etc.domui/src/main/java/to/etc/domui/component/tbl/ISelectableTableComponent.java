package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface ISelectableTableComponent<T> {
	boolean isMultiSelectionVisible();

	void setShowSelection(boolean on) throws Exception;

	@NonNull
	ITableModel<T> getModel();

	@Nullable
	ISelectionModel<T> getSelectionModel();

	/**
	 * If this component has a way to execute "select all", it should return a thing that does that. If
	 * it does not know it should return null.
	 * @return
	 */
	@Nullable
	ISelectionAllHandler getSelectionAllHandler();
}
