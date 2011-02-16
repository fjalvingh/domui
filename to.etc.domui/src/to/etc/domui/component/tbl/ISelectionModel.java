package to.etc.domui.component.tbl;

import javax.annotation.*;

public interface ISelectionModel<T> {
	boolean isMultiSelect();

	boolean isSelected(@Nonnull T rowinstance);

	int getSelectionCount();

	void setInstanceSelected(@Nonnull T rowinstance, boolean on) throws Exception;

	//	void toggleInstanceSet(ITableModel<T> model, int startindex, int endindex) throws Exception;

	void addListener(ISelectionListener<T> l);

	void removeListener(ISelectionListener<T> l);
}
