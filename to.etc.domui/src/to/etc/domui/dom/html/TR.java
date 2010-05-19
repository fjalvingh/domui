package to.etc.domui.dom.html;

import to.etc.domui.util.*;

public class TR extends NodeContainer implements IDraggable {
	private Object m_rowData;

	private IDragHandler m_dragHandler;

	public TR() {
		super("tr");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTR(this);
	}

	public Object getRowData() {
		return m_rowData;
	}

	public void setRowData(Object rowData) {
		m_rowData = rowData;
	}

	public TD addCell() {
		TD td = new TD();
		add(td);
		return td;
	}

	public TD addCell(String cssclass) {
		TD td = new TD();
		add(td);
		td.setCssClass(cssclass);
		return td;
	}


	@Override
	public IDragHandler getDragHandler() {
		return m_dragHandler;
	}

	@Override
	public void setDragHandler(IDragHandler dh) {
		m_dragHandler = dh;
	}
}
