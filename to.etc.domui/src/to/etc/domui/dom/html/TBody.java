package to.etc.domui.dom.html;

public class TBody extends NodeContainer {
	//	private IDropHandler			m_dropHandler;
	public TBody() {
		super("tbody");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTBody(this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions.									*/
	/*--------------------------------------------------------------*/
	//	private int		m_columnCount;
	private TR m_currentRow;

	private TD m_currentCell;

	/**
	 * Add a new row to the table.
	 * @return
	 */
	public TR addRow() {
		m_currentRow = new TR();
		add(m_currentRow);
		return m_currentRow;
	}

	public TD addCell() {
		m_currentCell = new TD();
		m_currentRow.add(m_currentCell);
		return m_currentCell;
	}

	public TD addCell(String css) {
		m_currentCell = new TD();
		m_currentRow.add(m_currentCell);
		m_currentCell.setCssClass(css);
		return m_currentCell;
	}

	public TD addRowAndCell() {
		addRow();
		return addCell();
	}

	public TD cell() {
		return m_currentCell;
	}

	public TR row() {
		return m_currentRow;
	}

	public TD nextRowCell() {
		addRow();
		return addCell();
	}
	//	/**
	//	 * {@inheritDoc}
	//	 * @see to.etc.domui.util.IDropTargetable#getDropHandler()
	//	 */
	//	public IDropHandler getDropHandler() {
	//		return m_dropHandler;
	//	}
	//	/**
	//	 * {@inheritDoc}
	//	 * @see to.etc.domui.util.IDropTargetable#setDropHandler(to.etc.domui.util.IDropHandler)
	//	 */
	//	public void setDropHandler(IDropHandler dropHandler) {
	//		m_dropHandler = dropHandler;
	//	}
}
