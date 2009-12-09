package to.etc.domui.component.ntbl;

import to.etc.domui.dom.html.*;

public interface IEditRowHandler<T, E extends NodeContainer & IEditor> extends IRowEditorFactory<T, E> {
	boolean onRowEditComplete(E editor, T instance) throws Exception;

	boolean onRowNewComplete(E editor, T instance) throws Exception;
}
