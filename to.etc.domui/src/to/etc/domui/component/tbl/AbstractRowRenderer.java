package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class AbstractRowRenderer {
	/** The class whose instances we'll render in this table. */
	private final Class<?> m_dataClass;

	final private ClassMetaModel m_metaModel;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	protected final List<SimpleColumnDef> m_columnList = new ArrayList<SimpleColumnDef>();

	private SimpleColumnDef m_sortColumn;

	private boolean m_sortDescending;

	private Img[] m_sortImages;

	private ICellClicked<?> m_rowClicked;

	public AbstractRowRenderer(Class<?> data) {
		m_dataClass = data;
		m_metaModel = MetaManager.findClassMeta(m_dataClass);
		m_sortDescending = model().getDefaultSortDirection() == SortableType.SORTABLE_DESC;
	}

	/**
	 * Returns the metamodel used.
	 * @return
	 */
	protected ClassMetaModel model() {
		return m_metaModel;
	}

	/**
	 * Returns the record type being rendered.
	 * @return
	 */
	protected Class<?> getActualClass() {
		return m_dataClass;
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	protected void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	/**
	 * Complete this object if it is not already complete (internal).
	 */
	protected void complete(final DataTable tbl) {
		m_completed = true;
	}

	/**
	 * Check if this object is used (completed) and thereby unmodifyable (internal).
	 * @return
	 */
	protected boolean isComplete() {
		return m_completed;
	}

	protected void setSortColumn(SimpleColumnDef cd) {
		m_sortColumn = cd;
	}

	protected SimpleColumnDef getSortColumn() {
		return m_sortColumn;
	}

	/**
	 * Return the definition for the nth column.
	 * @param ix
	 * @return
	 */
	public SimpleColumnDef getColumn(final int ix) {
		if(ix < 0 || ix >= m_columnList.size())
			throw new IndexOutOfBoundsException("Column " + ix + " does not exist (yet?)");
		return m_columnList.get(ix);
	}

	public void setColumnWidths(final String... widths) {
		check();
		int ix = 0;
		for(final String s : widths) {
			getColumn(ix++).setWidth(s);
		}
	}

	public void setColumnWidth(final int index, final String width) {
		check();
		getColumn(index).setWidth(width);
	}

	public void setNodeRenderer(final int index, final INodeContentRenderer<?> renderer) {
		check();
		getColumn(index).setContentRenderer(renderer);
	}

	public INodeContentRenderer<?> getNodeRenderer(final int index) {
		return getColumn(index).getContentRenderer();
	}

	public ICellClicked<?> getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * Sets a Click handler to use when the row is clicked.
	 * @param rowClicked
	 */
	public void setRowClicked(final ICellClicked<?> rowClicked) {
		m_rowClicked = rowClicked;
	}

	/**
	 * Get the cell clicked handler for the specified column.
	 * @param col
	 * @return
	 */
	public ICellClicked<?> getCellClicked(final int col) {
		return getColumn(col).getCellClicked();
	}

	/**
	 * Set the cell clicked handler for the specified column.
	 * @param col
	 * @param cellClicked
	 */
	public void setCellClicked(final int col, final ICellClicked<?> cellClicked) {
		getColumn(col).setCellClicked(cellClicked);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: the header.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Render the header for a table, using the simple column metadata provided. This renders a rich
	 * header, containing column labels, sort boxes and the like.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderHeader(to.etc.domui.component.tbl.HeaderContainer)
	 */
	public void renderHeader(final DataTable tbl, final HeaderContainer cc) throws Exception {
		m_sortImages = new Img[m_columnList.size()];
		int ix = 0;
		final boolean sortablemodel = tbl.getModel() instanceof ISortableTableModel;
		for(final SimpleColumnDef cd : m_columnList) {
			TH th;
			String label = cd.getColumnLabel();
			if(!cd.getSortable().isSortable() || !sortablemodel) {
				//-- Just add the label, if present,
				th = cc.add(label);
			} else {
				if(label == null || label.trim().length() == 0)
					label = "(unknown)";
				th = cc.add(label); // Add the label;
				th.setCssClass("ui-sortable");
				final SimpleColumnDef scd = cd;
				th.setClicked(new IClicked<TH>() {
					public void clicked(final TH b) throws Exception {
						handleSortClick(b, scd);
					}
				});

				//-- Add the sort order indicator: a single image containing either ^, v or both.
				final Img img = new Img();
				th.add(img);
				img.setBorder(0);
				//				img.setImgWidth(16);
				//				img.setImgHeight(16);
				if(cd == m_sortColumn) {
					img.setSrc(m_sortDescending ? "THEME/sort-desc.png" : "THEME/sort-asc.png");
				} else {
					img.setSrc("THEME/sort-none.png");
				}
				m_sortImages[ix] = img;
			}
			th.setWidth(cd.getWidth());
			ix++;
		}
	}

	void handleSortClick(final NodeBase nb, final SimpleColumnDef scd) throws Exception {
		//-- 1. Is this the same as the "current" sort column? If so toggle the sort order only.
		if(scd == m_sortColumn) {
			m_sortDescending = !m_sortDescending;
		} else {
			if(m_sortColumn != null)
				updateSortImage(m_sortColumn, "THEME/sort-none.png");

			m_sortColumn = scd; // Set the new sort column
			m_sortDescending = (scd.getSortable() == SortableType.SORTABLE_DESC); // Start sorting on default sort order
		}
		updateSortImage(scd, m_sortDescending ? "THEME/sort-desc.png" : "THEME/sort-asc.png");

		//-- Tell the model to sort
		final ISortableTableModel stm = (ISortableTableModel) nb.getParent(DataTable.class).getModel();
		stm.sortOn(scd.getPropertyName(), m_sortDescending);
	}

	private void updateSortImage(final SimpleColumnDef scd, final String img) {
		final int index = m_columnList.indexOf(scd);
		if(index == -1)
			throw new IllegalStateException("?? Cannot find sort column!?");
		m_sortImages[index].setSrc(img);
	}

	/**
	 * This gets called by the data table component just before it starts to render
	 * a new page. When called the query has not yet been done and nothing is rendered
	 * for this object. This exposes the actual model that will be used during the rendering
	 * process and allows this component to define sorting, if needed.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#beforeQuery(to.etc.domui.component.tbl.DataTable)
	 */
	public void beforeQuery(final DataTable tbl) throws Exception {
		complete(tbl);
		if(!(tbl.getModel() instanceof ISortableTableModel)) {
			//			m_sortableModel = false;
			return;
		}

		//		m_sortableModel = true;
		if(m_sortColumn == null)
			return;
		final ISortableTableModel stm = (ISortableTableModel) tbl.getModel();
		stm.sortOn(m_sortColumn.getPropertyName(), m_sortDescending);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: a row.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderRow(to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void renderRow(final DataTable tbl, final ColumnContainer cc, final int index, final Object instance) throws Exception {
		if(m_rowClicked != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every row. A single instance is OK too,
			 * provided it can calculate the row data from the TR it is attached to.
			 */
			cc.getTR().setClicked(new IClicked<TR>() {
				public void clicked(final TR b) throws Exception {
					((ICellClicked) getRowClicked()).cellClicked(tbl.getPage(), b, instance);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		}

		for(final SimpleColumnDef cd : m_columnList) {
			renderColumn(tbl, cc, index, instance, cd);
		}

		//-- Toggle odd/even indicator
		if((index & 1) == 0) {
			cc.getTR().removeCssClass("ui-odd");
			cc.getTR().addCssClass("ui-even");
		} else {
			cc.getTR().removeCssClass("ui-even");
			cc.getTR().addCssClass("ui-odd");
		}
	}

	/**
	 * Provides posibility of converion into rendering value. This method should be used as last resource rendering data conversion.
	 * @param index
	 * @param instance
	 * @param cd
	 * @param colVal
	 * @return string representation of colVal to be rendered.
	 */
	protected String provideStringValue(final int index, final Object instance, final SimpleColumnDef cd, final Object colVal) {
		return colVal.toString();
	}

	/**
	 * Render a single column fully.
	 * @param tbl
	 * @param cc
	 * @param index
	 * @param instance
	 * @param cd
	 * @throws Exception
	 */
	protected <X> void renderColumn(final DataTable tbl, final ColumnContainer cc, final int index, final Object instance, final SimpleColumnDef cd) throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		if(cd.getValueTransformer() == null)
			colval = (X) instance;
		else
			colval = (X) cd.getValueTransformer().getValue(instance);

		//-- Is a node renderer used?
		TD cell;
		if(null != cd.getContentRenderer()) {
			cell = cc.add((NodeBase) null); // Add the new row
			if(cd.getCssClass() != null)
				cell.addCssClass(cd.getCssClass());
			((INodeContentRenderer<Object>) cd.getContentRenderer()).renderNodeContent(tbl, cell, colval, instance); // %&*(%&^%*&%&( generics require casting here
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				if(cd.getValueConverter() != null)
					s = ((IConverter<X>) cd.getValueConverter()).convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = provideStringValue(index, instance, cd, colval);
			}

			if(s == null)
				cell = cc.add((NodeBase) null);
			else
				cell = cc.add(s);
			if(cd.getCssClass() != null)
				cell.addCssClass(cd.getCssClass());
			if(cd.isNowrap())
				cell.setNowrap(true);
		}

		//-- If a cellclicked thing is present attach it to the td
		if(cd.getCellClicked() != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every cell. A single instance is OK too,
			 * provided it can calculate the row and cell data from the TR it is attached to.
			 */
			cell.setClicked(new IClicked<TD>() {
				public void clicked(final TD b) throws Exception {
					((ICellClicked<Object>) cd.getCellClicked()).cellClicked(tbl.getPage(), b, instance);
				}
			});
			cell.addCssClass("ui-cellsel");
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cd.getCssClass() != null) {
			cell.addCssClass(cd.getCssClass());
		}
	}
}
