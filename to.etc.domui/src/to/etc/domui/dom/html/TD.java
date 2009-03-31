package to.etc.domui.dom.html;

public class TD extends NodeContainer {
	private TableVAlign		m_valign;
	private int				m_colspan = -1;
	private int				m_rowspan = -1;
	private boolean			m_nowrap;
	private String			m_cellHeight;
	private String			m_cellWidth;

	public TD() {
		super("td");
	}
	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitTD(this);
	}
	public Table	getTable() {
		return getParent(Table.class);
	}
	public TableVAlign getValign() {
		return m_valign;
	}
	public void setValign(TableVAlign valign) {
		m_valign = valign;
	}
	public int getColspan() {
		return m_colspan;
	}
	public void setColspan(int colspan) {
		m_colspan = colspan;
	}
	public int getRowspan() {
		return m_rowspan;
	}
	public void setRowspan(int rowspan) {
		m_rowspan = rowspan;
	}
	public boolean isNowrap() {
		return m_nowrap;
	}
	public void setNowrap(boolean nowrap) {
		m_nowrap = nowrap;
	}
	public String getCellHeight() {
		return m_cellHeight;
	}
	public void setCellHeight(String cellHeight) {
		m_cellHeight = cellHeight;
	}
	public String getCellWidth() {
		return m_cellWidth;
	}
	public void setCellWidth(String cellWidth) {
		m_cellWidth = cellWidth;
	}
}
