package to.etc.domui.component.ntbl;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;

public interface IEditorRowValidator<T> {
	public boolean onRowEditComplete(NodeContainer editor, ITableModel<T> model, T instance) throws Exception;

	public boolean onRowNewComplete(NodeContainer editor, ITableModel<T> model, T instance) throws Exception;
}
