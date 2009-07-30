package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * Renders rows from a datamodel; this tries to use the metadata for all
 * parts not explicitly specified.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public class SimpleRowRenderer implements IRowRenderer {
	/** The class whose instances we'll render in this table. */
	//	private Class<?>				m_dataClass;

	private List<SimpleColumnDef> m_columnList = new ArrayList<SimpleColumnDef>();

	private SimpleColumnDef m_sortColumn;

	private boolean m_sortDescending;

	private Img[] m_sortImages;

	private ICellClicked< ? > m_rowClicked;

	//	private boolean					m_sortableModel;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public SimpleRowRenderer(Class< ? > dataClass, String... cols) {
		ClassMetaModel cmm = MetaManager.findClassMeta(dataClass);
		if(cols.length != 0)
			initializeExplicitColumns(cmm, cols);
		else
			initializeDefaultColumns(cmm);

		//-- Is there a default sort thingy? Is that column present?
		String sort = cmm.getDefaultSortProperty();
		if(sort != null) {
			for(SimpleColumnDef scd : m_columnList) {
				if(scd.getPropertyName().equals(sort)) {
					m_sortColumn = scd;
					m_sortDescending = cmm.getDefaultSortDirection() == SortableType.SORTABLE_DESC;
					break;
				}
			}
		}
	}

	/**
	 * This initializes the ColumnList by auto-decoding all required data from the class and the
	 * list of columns specified. It uses metamodel info if present.
	 *
	 * @param clz
	 * @param cols
	 */
	protected void initializeExplicitColumns(ClassMetaModel cmm, String[] cols) {
		if(cols == null || cols.length == 0)
			throw new IllegalStateException("The list-of-columns is empty or null; I need at least one column to continue.");

		List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandProperties(cmm, cols);
		initialize(xdpl);
	}

	private int m_totwidth;

	/**
	 * Initialize, using the genericized table column set.
	 * @param clz
	 * @param xdpl
	 */
	private void initialize(List<ExpandedDisplayProperty> xdpl) {
		//-- For all properties in the list, use metadata to define'm
		int[] widths = new int[80];
		m_totwidth = 0;
		int ix = 0;
		addColumns(xdpl, widths);
		ix = 0;
		for(SimpleColumnDef scd : m_columnList) {
			int pct = (100 * widths[ix++]) / m_totwidth;
			scd.setWidth(pct + "%");
		}
	}

	/**
	 * Return the definition for the nth column.
	 * @param ix
	 * @return
	 */
	public SimpleColumnDef getColumn(int ix) {
		if(ix < 0 || ix >= m_columnList.size())
			throw new IndexOutOfBoundsException("Column " + ix + " does not exist (yet?)");
		return m_columnList.get(ix);
	}

	public void setColumnWidths(String... widths) {
		int ix = 0;
		for(String s : widths) {
			getColumn(ix++).setWidth(s);
		}
	}

	public void setColumnWidth(int index, String width) {
		getColumn(index).setWidth(width);
	}

	public void setNodeRenderer(int index, INodeContentRenderer< ? > renderer) {
		getColumn(index).setContentRenderer(renderer);
	}

	public INodeContentRenderer< ? > getNodeRenderer(int index) {
		return getColumn(index).getContentRenderer();
	}

	private void addColumns(List<ExpandedDisplayProperty> xdpl, int[] widths) {
		for(ExpandedDisplayProperty xdp : xdpl) {
			if(xdp instanceof ExpandedDisplayPropertyList) {
				//-- Flatten: call for subs recursively.
				ExpandedDisplayPropertyList xdl = (ExpandedDisplayPropertyList) xdp;
				addColumns(xdl.getChildren(), widths);
				continue;
			}

			//-- Create a column def from the metadata
			SimpleColumnDef scd = new SimpleColumnDef(xdp);
			int dl = xdp.getDisplayLength();
			if(dl <= 0)
				dl = 10;
			System.out.println("XDPL: property " + xdp.getName() + " size=" + dl);
			widths[m_columnList.size()] = dl;
			m_totwidth += dl;
			m_columnList.add(scd); // ORDER!

			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
			}
		}
	}

	/**
	 * Called for an empty column list, this uses table metadata to create a column list. If
	 * the metadata does not contain stuff this aborts.
	 * @param clz
	 */
	private void initializeDefaultColumns(ClassMetaModel cmm) {
		List<DisplayPropertyMetaModel> dpl = cmm.getTableDisplayProperties();
		if(dpl.size() == 0)
			throw new IllegalStateException("The list-of-columns to show is empty, and the class has no metadata (@MetaObject) defining a set of columns as default table columns, so there.");
		List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
		initialize(xdpl);
	}

	public ICellClicked< ? > getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * Sets a Click handler to use when the row is clicked.
	 * @param rowClicked
	 */
	public void setRowClicked(ICellClicked< ? > rowClicked) {
		m_rowClicked = rowClicked;
	}

	/**
	 * Get the cell clicked handler for the specified column.
	 * @param col
	 * @return
	 */
	public ICellClicked<?> getCellClicked(int col) {
		return getColumn(col).getCellClicked();
	}

	/**
	 * Set the cell clicked handler for the specified column.
	 * @param col
	 * @param cellClicked
	 */
	public void setCellClicked(int col, final ICellClicked<?> cellClicked) {
		getColumn(col).setCellClicked(cellClicked);
	}


	//	public boolean isSortableModel() {
	//		return m_sortableModel;
	//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: the header.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Render the header for a table, using the simple column metadata provided. This renders a rich
	 * header, containing column labels, sort boxes and the like.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderHeader(to.etc.domui.component.tbl.HeaderContainer)
	 */
	public void renderHeader(DataTable tbl, HeaderContainer cc) throws Exception {
		m_sortImages = new Img[m_columnList.size()];
		int ix = 0;
		boolean sortablemodel = tbl.getModel() instanceof ISortableTableModel;
		for(SimpleColumnDef cd : m_columnList) {
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
					public void clicked(TH b) throws Exception {
						handleSortClick(b, scd);
					}
				});

				//-- Add the sort order indicator: a single image containing either ^, v or both.
				Img img = new Img();
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

	void handleSortClick(NodeBase nb, SimpleColumnDef scd) throws Exception {
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
		ISortableTableModel stm = (ISortableTableModel) nb.getParent(DataTable.class).getModel();
		stm.sortOn(scd.getPropertyName(), m_sortDescending);
	}

	private void updateSortImage(SimpleColumnDef scd, String img) {
		int index = m_columnList.indexOf(scd);
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
	public void beforeQuery(DataTable tbl) throws Exception {
		if(!(tbl.getModel() instanceof ISortableTableModel)) {
			//			m_sortableModel = false;
			return;
		}

		//		m_sortableModel = true;
		if(m_sortColumn == null)
			return;
		ISortableTableModel stm = (ISortableTableModel) tbl.getModel();
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
	public void renderRow(final DataTable tbl, ColumnContainer cc, int index, final Object instance) throws Exception {
		if(m_rowClicked != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every row. A single instance is OK too,
			 * provided it can calculate the row data from the TR it is attached to.
			 */
			cc.getTR().setClicked(new IClicked<TR>() {
				public void clicked(TR b) throws Exception {
					((ICellClicked) getRowClicked()).cellClicked(tbl.getPage(), b, instance);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		}

		for(SimpleColumnDef cd : m_columnList) {
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
	 * Render a single column fully.
	 * @param tbl
	 * @param cc
	 * @param index
	 * @param instance
	 * @param cd
	 * @throws Exception
	 */
	protected <X> void renderColumn(final DataTable tbl, ColumnContainer cc, int index, final Object instance, final SimpleColumnDef cd) throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		if(cd.getValueTransformer() == null)
			colval = (X) instance;
		else
			colval = (X) cd.getValueTransformer().getValue(instance);

		//-- Is a node renderer used?
		TD	cell;
		if(null != cd.getContentRenderer()) {
			cell = cc.add((NodeBase) null); // Add the new row
			((INodeContentRenderer<Object>) cd.getContentRenderer()).renderNodeContent(tbl, cell, colval, instance); // %&*(%&^%*&%&( generics require casting here
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				if(cd.getValueConverter() != null)
					s = ((IConverter<X>) cd.getValueConverter()).convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = colval.toString();
			}

			if(s == null)
				cell = cc.add((NodeBase) null);
			else
				cell = cc.add(s);
		}

		//-- If a cellclicked thing is present attach it to the td
		if(cd.getCellClicked() != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every cell. A single instance is OK too,
			 * provided it can calculate the row and cell data from the TR it is attached to.
			 */
			cc.getTR().setClicked(new IClicked<TR>() {
				public void clicked(TR b) throws Exception {
					((ICellClicked<Object>) cd.getCellClicked()).cellClicked(tbl.getPage(), b, instance);
				}
			});
			cc.getTR().addCssClass("ui-cellsel");
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cd.getCssClass() != null) {
			cell.addCssClass(cd.getCssClass());
		}
	}

}
