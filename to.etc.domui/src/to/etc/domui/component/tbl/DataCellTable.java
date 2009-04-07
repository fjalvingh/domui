package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A table-related component which shows all "rows" in a single cell instead of
 * in a table. This results in an w x h grid where each cell in the grid contains
 * a single data item. Example is a photo album's index page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2008
 */
public class DataCellTable extends TabularComponentBase {
	private int					m_rows = 3, m_columns = 3;

	private Table				m_table = new Table();

	private TBody				m_dataBody;

	private boolean				m_renderEmptyCells;
	private boolean				m_renderEmptyRows;

	/** The specified ComboRenderer used. */
	private INodeContentRenderer<?>	m_contentRenderer;

	private INodeContentRenderer<?>	m_actualContentRenderer;

	private Class<? extends INodeContentRenderer<?>>	m_contentRendererClass;

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		if(rows == m_rows)
			return;
		m_rows = rows;
		rebuild();
	}

	public int getColumns() {
		return m_columns;
	}

	public void setColumns(int columns) {
		if(m_columns == columns)
			return;
		m_columns = columns;
		rebuild();
	}

	@Override
	int getPageSize() {
		return m_columns * m_rows;
	}

	public boolean isRenderEmptyCells() {
		return m_renderEmptyCells;
	}

	/**
	 * When set to T, empty cells (cells that have no data content, usually present at the
	 * end of the table in the last row) will be rendered by calling the INodeContentRenderer
	 * with a null object value. The default is to simply render empty (invisible) cells for
	 * the missing data items.
	 *
	 * @param renderEmptyCells
	 */
	public void setRenderEmptyCells(boolean renderEmptyCells) {
		m_renderEmptyCells = renderEmptyCells;
	}

	public INodeContentRenderer< ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer< ? > contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	public Class< ? extends INodeContentRenderer< ? >> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(Class< ? extends INodeContentRenderer< ? >> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}
	public boolean isRenderEmptyRows() {
		return m_renderEmptyRows;
	}
	public void setRenderEmptyRows(boolean renderEmptyRows) {
		m_renderEmptyRows = renderEmptyRows;
	}

	private void	rebuild() {
		forceRebuild();
	}

	private INodeContentRenderer<?> calculateContentRenderer(Object val) {
		if(m_actualContentRenderer != null)
			return m_actualContentRenderer;
		if(m_contentRenderer != null)
			return (m_actualContentRenderer = m_contentRenderer);
		if(m_contentRendererClass != null)
			return (m_actualContentRenderer = DomApplication.get().createInstance(m_contentRendererClass));
		throw new IllegalStateException("Missing INodeContentRenderer on "+this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Content (re)renderers.								*/
	/*--------------------------------------------------------------*/
	/**
	 * 
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dct");
		calcIndices();									// Calculate rows to show.

		//-- If we've nothing to show- show nothing beautifully.
		List<?>	list = getPageItems();					// Data to show
		if(list.size() == 0) {
			Div	error	= new Div();
			error.setCssClass("ui-dct-nores");
			error.setText(NlsContext.getGlobalMessage(Msgs.UI_DATATABLE_EMPTY));
			add(error);
			return;
		}
		m_table.removeAllChildren();
		add(m_table);
		m_dataBody = new TBody();
		m_table.add(m_dataBody);

		//-- Row- and column renderer thingerydoo.
		int	index = 0;
		for(int row = 0; row < getRows(); row++) {
			//-- Create the next row of cells,
			TR	tr	= new TR();
			m_dataBody.add(tr);

			//-- Do all columns,
			for(int col = 0; col < getColumns(); col++) {
				TD	td	= new TD();
				tr.add(td);

				Object	value = null;
				if(index >= list.size()) {
					if(! isRenderEmptyCells()) {
						//-- Empty cell node
						td.setCssClass("ui-dct-empty");
						continue;
					}
				} else
					value = list.get(index);

				//-- Call the renderer
				INodeContentRenderer<Object>	r = (INodeContentRenderer<Object>)calculateContentRenderer(value);
				r.renderNodeContent(this, td, value, null);
				index++;
			}

			//-- This-row has completed. Are there more cells?
			if(index >= list.size()) {
				if(! isRenderEmptyRows())
					break;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	TableModelListener interface implementation.		*/
	/*--------------------------------------------------------------*/
	/**
	 * 
	 * @see to.etc.domui.component.tbl.ITableModelListener#modelChanged(to.etc.domui.component.tbl.ITableModel)
	 */
	public void modelChanged(ITableModel<Object> model) {
		rebuild();
	}

	public void rowAdded(ITableModel<Object> model, int index, Object value) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void rowDeleted(ITableModel<Object> model, int index, Object value) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void rowModified(ITableModel<Object> model, int index, Object value) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
