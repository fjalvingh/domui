package to.etc.domui.component.tbl;

import javax.annotation.*;

public interface ISelectionModel<T, K> {
	int getMaxSelections();

	boolean isSelected(@Nonnull T rowinstance);

	int getSelectionCount();

	void setInstanceSelected(@Nonnull T rowinstance, boolean on);
}
