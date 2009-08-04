package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * Highly customizable RowRenderer. This has many ways to customize the row output, often using
 * metadata. The definition for this renderer can be set until it's first use; it's actual definition
 * gets calculated at the time it's first used.
 * The possible specifications used in property modifiers are:
 * <ul>
 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key to internationalize.</li>
 *	<li>"$cssclass": a String denoting a CSS class.</li>
 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
 *	<li>IConverter: an instance of a converter</li>
 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public class BasicRowRenderer implements IRowRenderer {
	static public final String NOWRAP = "-NOWRAP";

	/** The class whose instances we'll render in this table. */
	private final Class< ? > m_dataClass;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	private final List<SimpleColumnDef> m_columnList = new ArrayList<SimpleColumnDef>();

	private SimpleColumnDef m_sortColumn;

	private boolean m_sortDescending;

	private Img[] m_sortImages;

	private ICellClicked< ? > m_rowClicked;

	private final String m_sortColumnName;

	//	private boolean					m_sortableModel;

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public BasicRowRenderer(final Class< ? > dataClass, final Object... cols) throws Exception {
		m_dataClass = dataClass;
		if(cols.length != 0)
			addColumns(cols);
		ClassMetaModel cmm = MetaManager.findClassMeta(m_dataClass);
		m_sortColumnName = cmm.getDefaultSortProperty();
		m_sortDescending = cmm.getDefaultSortDirection() == SortableType.SORTABLE_DESC;
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	protected void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	/**
	 * Add the specified list of property names and presentation options to the column definitions. The items passed in the
	 * columns object can be multiple property definitions followed by specifications. A property name is a string starting
	 * with a letter always. All other Strings and objects are treated as specifications for display. The possible specifications
	 * are:
	 * <ul>
	 *	<li>"%28": a String starting with % denotes a width in percents. %28 gets translated to setWidth("28%");</li>
	 *	<li>"^Title": a String starting with ^ denotes the header caption to use. Use ^~key~ to internationalize.</li>
	 *	<li>"$cssclass": a String denoting a CSS class.</li>
	 *	<li>Class&lt;? extends IConverter&gt;: the converter to use to convert the value to a string</li>
	 *	<li>IConverter: an instance of a converter</li>
	 *	<li>Class&lt;? extends INodeContentRenderer&lt;T&gt;&gt;: the class to use to render the content of the column.</li>
	 *	<li>INodeContentRenderer&lt;T&gt;: an instance of a node renderer to use to render the content of the column.</li>
	 *	<li>BasicRowRenderer.NOWRAP: forces a 'nowrap' on the column</li>
	 * </ul>
	 *
	 * @param clz
	 * @param cols
	 */
	@SuppressWarnings("fallthrough")
	public <X, C extends IConverter<X>, R extends INodeContentRenderer<X>> BasicRowRenderer addColumns(final Object... cols) throws Exception {
		check();
		if(cols == null || cols.length == 0)
			throw new IllegalStateException("The list-of-columns is empty or null; I need at least one column to continue.");
		String property = null;
		String width = null;
		C conv = null;
		Class<C> convclz = null;
		String caption = null;
		String cssclass = null;
		boolean nowrap = false;
		R nodeRenderer = null;
		Class<R> nrclass = null;

		for(Object val : cols) {
			if(property == null) { // Always must start with a property.
				if(!(val instanceof String))
					throw new IllegalArgumentException("Expecting a 'property' path expression, not a " + val);
				property = (String) val;
			} else if(NOWRAP == val) {
				nowrap = true;
			} else if(val instanceof String) {
				String s = (String) val;
				char c = s.length() == 0 ? 0 : s.charAt(0); // The empty string is used to denote a node renderer that takes the entire record as a parameter
				switch(c){
					default:
						if(!Character.isLetter(c))
							throw new IllegalArgumentException("Unexpected 'string' parameter: '" + s + "'");
						//-- FALL THROUGH
					case 0:
						internalAddProperty(property, width, conv, convclz, caption, cssclass, nodeRenderer, nrclass, nowrap);
						property = s;
						width = null;
						conv = null;
						convclz = null;
						caption = null;
						cssclass = null;
						nodeRenderer = null;
						nrclass = null;
						nowrap = true;
						break;

					case '%':
						//-- Width specification, in percents;
						width = s.substring(1) + "%";
						break;
					case '$':
						cssclass = s.substring(1);
						break;
					case '^':
						caption = DomUtil.nlsLabel(s.substring(1));
						break;
				}
			} else if(val instanceof IConverter< ? >)
				conv = (C) val;
			else if(val instanceof INodeContentRenderer< ? >)
				nodeRenderer = (R) val;
			else if(val instanceof Class< ? >) {
				Class< ? > c = (Class< ? >) val;
				if(INodeContentRenderer.class.isAssignableFrom(c))
					nrclass = (Class<R>) c;
				else if(IConverter.class.isAssignableFrom(c))
					convclz = (Class<C>) c;
				else
					throw new IllegalArgumentException("Invalid 'class' argument: " + c);
			} else
				throw new IllegalArgumentException("Invalid column modifier argument: " + val);
		}
		internalAddProperty(property, width, conv, convclz, caption, cssclass, nodeRenderer, nrclass, nowrap);
		return this;
	}

	private INodeContentRenderer< ? > tryRenderer(final INodeContentRenderer< ? > nodeRenderer, final Class< ? extends INodeContentRenderer< ? >> nrclass) throws Exception {
		if(nodeRenderer != null) {
			if(nrclass != null)
				throw new IllegalArgumentException("Both a NodeContentRenderer instance AND a class specified: " + nodeRenderer + " + " + nrclass);
			return nodeRenderer;
		}
		if(nrclass == null)
			return null;
		return DomApplication.get().createInstance(nrclass);
	}

	private <X, T extends IConverter<X>> T tryConverter(final Class<T> cclz, final T ins) {
		if(cclz != null) {
			if(ins != null)
				throw new IllegalArgumentException("Both a IConverter class AND an instance specified: " + cclz + " and " + ins);
			return ConverterRegistry.getConverter(cclz);
		}
		return ins;
	}

	/**
	 * Internal worker to add a field using the specified optional modifiers.
	 * @param property
	 * @param width
	 * @param conv
	 * @param convclz
	 * @param caption
	 * @param cssclass
	 * @param nodeRenderer
	 * @param nrclass
	 */
	private <X, C extends IConverter<X>, R extends INodeContentRenderer<X>> void internalAddProperty(final String property, final String width, final C conv, final Class<C> convclz,
		final String caption, final String cssclass, final R nodeRenderer, final Class<R> nrclass, final boolean nowrap) throws Exception {
		if(property == null)
			throw new IllegalStateException("? property name is empty?!");

		/*
		 * If this is propertyless we need to add a column directly, and use it to assign to.
		 */
		if(property.length() == 0) {
			SimpleColumnDef cd = new SimpleColumnDef();
			m_columnList.add(cd);
			cd.setColumnLabel(caption);
			cd.setColumnType(m_dataClass); // By definition, the data value is the record instance,
			cd.setContentRenderer(tryRenderer(nodeRenderer, nrclass));
			cd.setPropertyName("");
			cd.setValueConverter(tryConverter(convclz, conv));
			cd.setWidth(width);
			cd.setCssClass(cssclass);
			cd.setNowrap(nowrap);
			return;
		}

		//-- Property must refer a property, so get it;
		ClassMetaModel cmm = MetaManager.findClassMeta(m_dataClass);
		PropertyMetaModel pmm = cmm.findProperty(property);
		if(pmm == null)
			throw new IllegalArgumentException("Undefined property path: '" + property + "' in classModel=" + cmm);

		//-- If a NodeRenderer is present we always use that, so property expansion is unwanted.
		INodeContentRenderer< ? > ncr = tryRenderer(nodeRenderer, nrclass);
		if(ncr != null) {
			SimpleColumnDef cd = new SimpleColumnDef();
			m_columnList.add(cd);
			cd.setValueTransformer(pmm.getAccessor());
			cd.setColumnLabel(caption);
			cd.setColumnType(pmm.getActualType());
			cd.setContentRenderer(tryRenderer(nodeRenderer, nrclass));
			cd.setPropertyName(property);
			cd.setValueConverter(tryConverter(convclz, conv)); // FIXME Not used as per the definition on content renderers??
			cd.setWidth(width);
			cd.setCssClass(cssclass);
			cd.setNowrap(nowrap);
			if(pmm.getNumericPresentation() != null && pmm.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				cd.setCssClass("ui-numeric");
			}
			return;
		}

		//-- This is a property to display. Expand it into DisplayProperties to get the #of columns to append.
		ExpandedDisplayProperty xdpt = ExpandedDisplayProperty.expandProperty(pmm);
		List<ExpandedDisplayProperty> flat = new ArrayList<ExpandedDisplayProperty>();
		ExpandedDisplayProperty.flatten(flat, xdpt); // Expand any compounds;

		//-- If we have >1 columns here we cannot apply many of the parameters, so error on them
		if(flat.size() > 1) {
			if(width != null)
				throw new IllegalStateException("Cannot apply a WIDTH to a multicolumn property: " + pmm);
			if(conv != null || convclz != null)
				throw new IllegalStateException("Cannot apply an IConverter to a multicolumn property: " + pmm);
			if(caption != null)
				throw new IllegalStateException("Cannot apply a caption to a multicolumn property: " + pmm);
		}

		//-- And finally: add all columns ;-)
		for(ExpandedDisplayProperty xdp : flat) {
			if(xdp.getName() == null)
				throw new IllegalStateException("All columns MUST have some name");

			//-- Create a column def from the metadata
			SimpleColumnDef scd = new SimpleColumnDef(xdp);
			m_columnList.add(scd);
			scd.setDisplayLength(xdp.getDisplayLength());
			if(width != null)
				scd.setWidth(width);
			if(cssclass != null)
				scd.setCssClass(cssclass);
			scd.setColumnLabel(caption == null ? xdp.getDefaultLabel() : caption);
			scd.setColumnType(xdp.getActualType());
			scd.setValueTransformer(xdp.getAccessor()); // Thing which can obtain the value from the property
			scd.setValueConverter(tryConverter(convclz, conv));
			if(scd.getValueConverter() == null && xdp.getConverterClass() != null)
				scd.setValueConverter(ConverterRegistry.getConverter(xdp.getConverterClass()));
			if(scd.getValueConverter() == null) {
				/*
				 * Try to get a converter for this, if needed.
				 */
				if(xdp.getActualType() != String.class) {
					IConverter<?> c = ConverterRegistry.getConverter(xdp.getActualType(), xdp);
					scd.setValueConverter(c);
				}
			}
			scd.setSortable(SortableType.UNSORTABLE); // FIXME From meta pls
			scd.setSortable(xdp.getSortable());
			scd.setPropertyName(xdp.getName());
			scd.setNowrap(nowrap);
			scd.setNumericPresentation(xdp.getNumericPresentation());
			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
			}
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

	public void setColumnWidths(final String... widths) {
		check();
		int ix = 0;
		for(String s : widths) {
			getColumn(ix++).setWidth(s);
		}
	}

	public void setColumnWidth(final int index, final String width) {
		check();
		getColumn(index).setWidth(width);
	}

	public void setNodeRenderer(final int index, final INodeContentRenderer< ? > renderer) {
		check();
		getColumn(index).setContentRenderer(renderer);
	}

	public INodeContentRenderer< ? > getNodeRenderer(final int index) {
		return getColumn(index).getContentRenderer();
	}

	public ICellClicked< ? > getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * Sets a Click handler to use when the row is clicked.
	 * @param rowClicked
	 */
	public void setRowClicked(final ICellClicked< ? > rowClicked) {
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

	/**
	 * Complete this object if it is not already complete.
	 */
	private void complete(final DataTable tbl) {
		if(m_completed)
			return;

		//-- If we have no columns at all we use a default column list.
		if(m_columnList.size() == 0) {
			ClassMetaModel cmm = MetaManager.findClassMeta(m_dataClass);
			List<DisplayPropertyMetaModel> dpl = cmm.getTableDisplayProperties();
			if(dpl.size() == 0)
				throw new IllegalStateException("The list-of-columns to show is empty, and the class " + m_dataClass
					+ " has no metadata (@MetaObject) definition defining a set of columns as default table columns, so there.");
			List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
			xdpl = ExpandedDisplayProperty.flatten(xdpl); // Flatten the list: expand any compounds.
			for(ExpandedDisplayProperty xdp : xdpl)
				m_columnList.add(new SimpleColumnDef(xdp));
		}

		//-- Is there a default sort thingy? Is that column present?
		if(m_sortColumnName != null) {
			for(SimpleColumnDef scd : m_columnList) {
				if(scd.getPropertyName().equals(m_sortColumnName)) {
					m_sortColumn = scd;
					break;
				}
			}
		}

		/*
		 * Width calculations. This tries to assign widths to columns that have no explicit width assigned. It starts
		 * by calculating all assigned widths in percents and in pixels. It then calculates widths for the columns that
		 * have no widths assigned.
		 */
		//-- Loop 1: calculate current size allocations for columns that have a width assigned.
		int totpct = 0;
		int totpix = 0;
		int ntoass = 0; // #columns that need a width
		int totdw = 0; // Total display width of all unassigned columns.
		for(SimpleColumnDef scd : m_columnList) {
			if(scd.getWidth() == null || scd.getWidth().length() == 0) {
				ntoass++;
				totdw += scd.getDisplayLength();
			} else {
				String s = scd.getWidth().trim();
				if(s.endsWith("%")) {
					int w = StringTool.strToInt(s.substring(0, s.length() - 1), -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width percentage: " + s + " for presentation column " + scd.getPropertyName());
					totpct += w;
				} else {
					//-- Should be numeric width, in pixels,
					int w = StringTool.strToInt(s, -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width #pixels: " + s + " for presentation column " + scd.getPropertyName());
					totpix += w;
				}
			}
		}

		//-- Is there something to assign, and are the numbers reasonable? If so calculate...
		int pixwidth = 1280;
		if(ntoass > 0 && totpct < 100 && totpix < pixwidth) {
			int pctleft = 100 - totpct; // How many percents left?
			if(pctleft == 100 && totpix > 0) {
				//-- All widths assigned in pixels... Calculate a percentage of the #pixels left
				pctleft = (100 * (pixwidth - totpix)) / pixwidth;
			}

			//-- Reassign the percentage left over all unassigned columns.
			for(SimpleColumnDef scd : m_columnList) {
				if(scd.getWidth() == null || scd.getWidth().length() == 0) {
					//-- Calculate a size factor, then use it to assign
					double fact = (double) scd.getDisplayLength() / (double) totdw;
					int pct = (int) (fact * pctleft + 0.5);
					scd.setWidth(pct + "%");
				}
			}
		}

		m_completed = true;
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
					public void clicked(final TH b) throws Exception {
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
		ISortableTableModel stm = (ISortableTableModel) nb.getParent(DataTable.class).getModel();
		stm.sortOn(scd.getPropertyName(), m_sortDescending);
	}

	private void updateSortImage(final SimpleColumnDef scd, final String img) {
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
	public void beforeQuery(final DataTable tbl) throws Exception {
		complete(tbl);
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
	 * Provides posibility of converion into rendering value. This method should be used as last resource rendering data conversion.
	 * @param index
	 * @param instance
	 * @param cd
	 * @param colVal
	 * @return string representation of colVal to be rendered.
	 */
	protected String provideStringValue(int index, final Object instance, final SimpleColumnDef cd, final Object colVal) {
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
		TD	cell;
		if(null != cd.getContentRenderer()) {
			cell = cc.add((NodeBase) null); // Add the new row
			if(cd.getCssClass() != null)
				cell.addCssClass(cd.getCssClass());
			((INodeContentRenderer<Object>) cd.getContentRenderer()).renderNodeContent(tbl, cell, colval, instance); // %&*(%&^%*&%&( generics require casting here
			return;
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				if(cd.getValueConverter() != null)
					s = ((IConverter<X>) cd.getValueConverter()).convertObjectToString(NlsContext.getLocale(), colval);
				else if(colval instanceof String)
					s = (String) colval;
				else {
					s = provideStringValue(index, instance, cd, colval);
				}
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
				public void clicked(TD b) throws Exception {
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
