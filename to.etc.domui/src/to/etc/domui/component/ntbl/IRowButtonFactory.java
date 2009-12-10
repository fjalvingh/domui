package to.etc.domui.component.ntbl;

import to.etc.domui.component.tbl.*;

public interface IRowButtonFactory<T> {
	void addButtonsFor(RowButtonContainer c, T data) throws Exception;
}
