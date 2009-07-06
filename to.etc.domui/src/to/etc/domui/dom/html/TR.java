package to.etc.domui.dom.html;

public class TR extends NodeContainer {
	private Object m_rowData;

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

}
