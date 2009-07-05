package to.etc.domui.component.tbl;

import to.etc.domui.dom.html.*;

public interface ICellClicked<T> {
	public void cellClicked(Page pg, NodeBase tr, T rowval) throws Exception;
}
