package to.etc.domui.dom.html;

import to.etc.domui.util.*;

public class TD extends NodeContainer {
	private TableVAlign m_valign;

	private int m_colspan = -1;

	private int m_rowspan = -1;

	private boolean m_nowrap;

	private String m_cellHeight;

	private String m_cellWidth;

	private TDAlignType m_align;

	public TD() {
		super("td");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTD(this);
	}

	public Table getTable() {
		return getParent(Table.class);
	}

	public TableVAlign getValign() {
		return m_valign;
	}

	public void setValign(TableVAlign valign) {
		if(m_valign == valign)
			return;
		m_valign = valign;
		changed();
	}

	public int getColspan() {
		return m_colspan;
	}

	public void setColspan(int colspan) {
		if(m_colspan == colspan)
			return;
		m_colspan = colspan;
		changed();
	}

	public int getRowspan() {
		return m_rowspan;
	}

	public void setRowspan(int rowspan) {
		if(m_rowspan == rowspan)
			return;
		m_rowspan = rowspan;
		changed();
	}

	public boolean isNowrap() {
		return m_nowrap;
	}

	public void setNowrap(boolean nowrap) {
		if(m_nowrap == nowrap)
			return;
		m_nowrap = nowrap;
		changed();
	}

	public String getCellHeight() {
		return m_cellHeight;
	}

	public void setCellHeight(String cellHeight) {
		if(DomUtil.isEqual(m_cellHeight, cellHeight))
			return;
		m_cellHeight = cellHeight;
		changed();
	}

	public String getCellWidth() {
		return m_cellWidth;
	}

	public void setCellWidth(String cellWidth) {
		if(DomUtil.isEqual(m_cellWidth, cellWidth))
			return;
		m_cellWidth = cellWidth;
		changed();
	}

	public TDAlignType getAlign() {
		return m_align;
	}

	public void setAlign(TDAlignType align) {
		if(m_align == align)
			return;
		m_align = align;
		changed();
	}
}
