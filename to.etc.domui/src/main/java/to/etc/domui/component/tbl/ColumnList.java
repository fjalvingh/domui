package to.etc.domui.component.tbl;

import kotlin.reflect.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.annotations.*;

import javax.annotation.*;
import java.util.*;

/**
 * A list of columns defined in a new-style row renderer.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2014
 */
public class ColumnList<T> implements Iterable<ColumnDef<T, ? >> {
	@Nonnull
	final private ClassMetaModel m_metaModel;

	@Nonnull
	final private List<ColumnDef< T, ? >> m_columnList = new ArrayList<ColumnDef< T, ? >>();

	@Nullable
	private ColumnDef< T, ? > m_sortColumn;

	private boolean m_sortDescending;

	@Nonnull
	final private Class<T> m_actualClass;

	public ColumnList(@Nonnull Class<T> rootClass, @Nonnull ClassMetaModel cmm) {
		m_actualClass = rootClass;
		m_metaModel = cmm;
		m_sortDescending = cmm.getDefaultSortDirection() == SortableType.SORTABLE_DESC;
	}

	public int size() {
		return m_columnList.size();
	}

	public void add(@Nonnull ColumnDef< T, ? > cd) {
		if(null == cd)
			throw new IllegalArgumentException("Cannot be null");
		m_columnList.add(cd);
	}

	@Nonnull
	private ClassMetaModel model() {
		return m_metaModel;
	}

	@Nonnull
	public ColumnDef< T, ? > get(int ix) {
		if(ix < 0 || ix >= m_columnList.size())
			throw new IndexOutOfBoundsException("Column " + ix + " does not exist");
		return m_columnList.get(ix);
	}

	/**
	 * Set the default sort column by property name. If it is null the default sort is undone.
	 * @param sort
	 */
	public void setDefaultSortColumn(@Nullable String sort) {
		if(null == sort) {
			m_sortColumn = null;
		} else {
			for(final ColumnDef< T, ? > scd : m_columnList) {
				if(DomUtil.isEqual(scd.getPropertyName(), sort)) {
					setSortColumn(scd, scd.getSortable());
					break;
				}
			}
		}
	}

	public void setSortColumn(@Nullable ColumnDef< T, ? > cd, @Nullable SortableType type) {
		m_sortColumn = cd;
		m_sortDescending = type == SortableType.SORTABLE_DESC;
	}

	public void setSortColumn(@Nullable ColumnDef< T, ? > cd) {
		m_sortColumn = cd;
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		final List<DisplayPropertyMetaModel> dpl = m_metaModel.getTableDisplayProperties();
		if(dpl.size() == 0)
			throw new IllegalStateException("The list-of-columns to show is empty, and the class " + m_metaModel.getActualClass()
				+ " has no @MetaObject definition defining a set of columns as default table columns, so there.");
		List<ExpandedDisplayProperty< ? >> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, m_metaModel, null);
		xdpl = ExpandedDisplayProperty.flatten(xdpl); // Flatten the list: expand any compounds.
		for(final ExpandedDisplayProperty< ? > xdp : xdpl) {
			addExpandedDisplayProp(xdp);
		}
	}

	@Nonnull
	private <V> ColumnDef<T, V> addExpandedDisplayProp(@Nonnull ExpandedDisplayProperty<V> xdp) {
		ColumnDef<T, V> scd = new ColumnDef<T, V>(this, xdp);
		if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
			scd.css("ui-numeric");
			scd.cssHeader("ui-numeric");
		}

		m_columnList.add(scd);
		return scd;
	}

	enum WidthType {
		Chars, Pct, NONE, Pixels
	}

	private final class ColSize {
		private ColumnDef<T, ?> m_col;

		private int m_size;

		private WidthType m_type;

		public ColSize(ColumnDef<T, ?> col, int size, WidthType type) {
			m_col = col;
			m_size = size;
			m_type = type;
		}

		public ColumnDef<T, ?> getCol() {
			return m_col;
		}

		public int getSize() {
			return m_size;
		}

		public WidthType getType() {
			return m_type;
		}
	}

	final private class Cols {
		private final List<ColSize> m_list;

		private final WidthType m_type;

		private final int m_winnerSize;

		public Cols(List<ColSize> list, WidthType type, int winnerSize) {
			m_list = list;
			m_type = type;
			m_winnerSize = winnerSize;
		}

		public List<ColSize> getList() {
			return m_list;
		}

		public WidthType getType() {
			return m_type;
		}

		public int getWinnerSize() {
			return m_winnerSize;
		}
	}

	private Cols getColSizes() {
		List<ColSize> l = new ArrayList<>();
		int nchar = 0;
		int npct = 0;
		int npix = 0;
		int totpct = 0;
		int totpix = 0;
		int totchar = 0;
		for(final ColumnDef<T, ? > scd : m_columnList) {
			int count = 0;
			WidthType wt = WidthType.NONE;
			String cwidth = scd.getWidth();
			if(null != cwidth)
				cwidth = cwidth.trim();
			if(cwidth == null || cwidth.length() == 0) {
				int chars = scd.getCharacterWidth();
				if(chars > 0) {
					wt = WidthType.Chars;
					count = chars;
					nchar++;
					totchar += chars;
				}
			} else {
				int pct = getPct(cwidth);
				if(pct > 0) {
					wt = WidthType.Pct;
					count = pct;
					npct++;
					totpct += pct;
				} else {
					int px = getPx(cwidth);
					if(px > 0) {
						npix++;
						totpix += px;
						wt = WidthType.Pixels;
						count = px;
					}
				}
			}
			l.add(new ColSize(scd, count, wt));

		}

		if(nchar >= npct && nchar >= npix) {
			return new Cols(l, WidthType.Chars, totchar);
		} else if(npct > nchar && npct > npix) {
			return new Cols(l, WidthType.Pct, totpct);
		} else if(npix > nchar && npix > npct) {
			return new Cols(l, WidthType.Pixels, totpix);
		} else
			throw new IllegalStateException("? No winner in calculation assignment");
	}

	/**
	 *
	 */
	public void calculateWidths() {
		Cols c = getColSizes();

		switch(c.getType()) {
			default:
				throw new IllegalStateException(c.getType() + " ?");

			case Chars:
				calculateCharWidths(c.getList(), c.getWinnerSize());
				break;
			case Pct:
				calculateCharWidths(c.getList(), c.getWinnerSize());
				break;
			case Pixels:
				calculateCharWidths(c.getList(), c.getWinnerSize());
				break;
		}
	}

	/**
	 * Assign all columns except the last one a size in em's.
	 */
	private void calculateCharWidths(List<ColSize> cols, int total) {
		for(int i = 0; i < cols.size()-1; i++) {
			ColSize col = cols.get(i);
			if(col.getType() == WidthType.Chars) {

				double size = col.getSize() * 0.50;
				col.getCol().setRenderedCellWidth(Math.round(size) + "em");
			} else if(col.getType() == WidthType.Pct) {
				col.getCol().setRenderedCellWidth(col.getSize() + "%");
			} else if(col.getType() == WidthType.Pixels) {
				col.getCol().setRenderedCellWidth(col.getSize() + "px");
			} else {
				col.getCol().setRenderedCellWidth("10em");
			}
		}

		//-- Last one must be auto
		if(cols.size() > 0) {
			cols.get(cols.size() - 1).getCol().setRenderedCellWidth("auto");
		}
	}

	private int getPx(String s) {
		if(! s.endsWith("px"))
			return -1;
		return StringTool.strToInt(s.substring(0, s.length() - 2).trim(), -1);
	}

	private int getPct(String s) {
		if(! s.endsWith("%"))
			return -1;
		return StringTool.strToInt(s.substring(0, s.length() - 1).trim(), -1);
	}


	///**
	// * Width calculations: this tries to assign widths to columns that have no explicit width assigned. It starts
	// * by calculating all assigned widths in percents and in pixels. It then calculates widths for the columns that
	// * have no widths assigned.
	// */
	//public void assignPercentages() {
	//	//-- Loop 1: calculate current size allocations for columns that have a width assigned.
	//	int totpct = 0;
	//	int totpix = 0;
	//	int ntoass = 0; // #columns that need a width
	//	int totdw = 0; // Total display width of all unassigned columns.
	//	for(final ColumnDef<T, ? > scd : m_columnList) {
	//		String cwidth = scd.getWidth();
	//		if(cwidth == null || cwidth.length() == 0) {
	//			ntoass++;
	//			totdw += scd.getEmWidth();
	//		} else {
	//			int pct = getPct(cwidth);
	//			if(pct > 0)
	//				totpct += pct;
	//			else {
	//				int px = getPx(cwidth);
	//				if(px > 0)
	//					totpix += px;
	//			}
	//		}
	//	}
	//
	//	//-- Is there something to assign, and are the numbers reasonable? If so calculate...
	//	final int pixwidth = 1280;
	//	if(ntoass > 0 && totpct < 100 && totpix < pixwidth) {
	//		int pctleft = 100 - totpct; // How many percents left?
	//		if(pctleft == 100 && totpix > 0) {
	//			//-- All widths assigned in pixels... Calculate a percentage of the #pixels left
	//			pctleft = (100 * (pixwidth - totpix)) / pixwidth;
	//		}
	//
	//		//-- Reassign the percentage left over all unassigned columns. Do it streaming, to ensure we reach 100%
	//		for(final ColumnDef<T, ? > scd : m_columnList) {
	//			String width = scd.getWidth();
	//			if(width == null || width.length() == 0) {
	//				//-- Calculate a size factor, then use it to assign
	//				final double fact = (double) scd.getEmWidth() / (double) totdw;
	//				final int pct = (int) (fact * pctleft + 0.5);
	//				pctleft -= pct;
	//				totdw -= scd.getEmWidth();
	//				scd.width(pct + "%");
	//			}
	//		}
	//	}
	//}

	/**
	 * Return the iterator for all elements.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@Nonnull
	public Iterator<ColumnDef<T, ? >> iterator() {
		return m_columnList.iterator();
	}

	public int indexOf(@Nonnull ColumnDef<T, ? > scd) {
		return m_columnList.indexOf(scd);
	}

	@Nullable
	public ColumnDef<T, ? > getSortColumn() {
		return m_sortColumn;
	}

	protected void updateDefaultSort(@Nonnull ColumnDef<T, ? > scd) {
		if(m_sortColumn == scd)
			m_sortDescending = scd.getSortable() == SortableType.SORTABLE_DESC;
	}

	public boolean isSortDescending() {
		return m_sortDescending;
	}

	public void setSortDescending(boolean desc) {
		m_sortDescending = desc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Typeful column definition code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add and return the column definition for a column on the specified property. Because Java still has no
	 * first-class properties (sigh) you need to pass in the property's type to get a typeful column. If you
	 * do not need a typeful column use {@link #column(String)}.
	 * @param type
	 * @param property
	 * @return
	 */
	@Nonnull
	public <V> ColumnDef<T, V> column(@Nonnull Class<V> type, @Nonnull @GProperty String property) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) model().getProperty(property);
		return createColumnDef(pmm);
	}

	@Nonnull
	private <V> ColumnDef<T, V> createColumnDef(@Nonnull PropertyMetaModel<V> pmm) {
		ColumnDef<T, V> scd = new ColumnDef<T, V>(this, pmm);
		scd.nowrap();
		add(scd);
		return scd;
	}

	/**
	 * This adds a column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 * @param property
	 * @return
	 */
	@Nonnull
	public ColumnDef<T, ? > column(@Nonnull @GProperty String property) {
		PropertyMetaModel< ? > pmm = model().getProperty(property);			// Get the appropriate model
		return createColumnDef(pmm);
	}

	public <F> ColumnDef<T, F> column(@Nonnull KProperty1<T, F> property) {
		PropertyMetaModel<F> pmm = (PropertyMetaModel<F>) model().getProperty(property.getName());
		return createColumnDef(pmm);
	}

	public <A, B> ColumnDef<T, B> column(@Nonnull KProperty1<T, A> property1, @Nonnull KProperty1<A, B> property2) {
		PropertyMetaModel<B> pmm = (PropertyMetaModel<B>) model().getProperty(property1.getName() + "." + property2.getName());
		return createColumnDef(pmm);
	}

	public <A, B, C> ColumnDef<T, C> column(@Nonnull KProperty1<T, A> property1, @Nonnull KProperty1<A, B> property2, KProperty1<B, C> property3) {
		PropertyMetaModel<C> pmm = (PropertyMetaModel<C>) model().getProperty(property1.getName()
			+ "." + property2.getName()
			+ "." + property3.getName()
		);
		return createColumnDef(pmm);
	}


	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 * @return
	 */
	@Nonnull
	public ColumnDef<T, T> column() {
		ColumnDef<T, T> scd = new ColumnDef<T, T>(this, m_actualClass);
		add(scd);
		scd.nowrap();
		return scd;
	}

//	/**
//	 *
//	 * @param clz
//	 * @param property
//	 * @return
//	 */
//	@Nonnull
//	public <V> ExpandedColumnDef<V> expand(@Nonnull Class<V> clz, @Nonnull @GProperty String property) {
//		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) model().getProperty(property);
//		return createExpandedColumnDef(pmm);
//	}
//
//	/**
//	 * This adds an expanded column on the specified property, but has no idea about the real type. It can be used as long
//	 * as that type is not needed.
//	 * @param property
//	 * @return
//	 */
//	@Nonnull
//	public ExpandedColumnDef< ? > expand(@Nonnull @GProperty String property) {
//		PropertyMetaModel< ? > pmm = model().getProperty(property);			// Get the appropriate model
//		return createExpandedColumnDef(pmm);
//	}
//
//	/**
//	 * This gets called when the property is to be expanded.
//	 * @param pmm
//	 * @return
//	 */
//	@Nonnull
//	private <V> ExpandedColumnDef<V> createExpandedColumnDef(@Nonnull PropertyMetaModel<V> pmm) {
//		//-- Try to see what the column expands to
//		final ExpandedDisplayProperty< ? > xdpt = ExpandedDisplayProperty.expandProperty(pmm);
//		final List<ExpandedDisplayProperty< ? >> flat = new ArrayList<ExpandedDisplayProperty< ? >>();
//		ExpandedDisplayProperty.flatten(flat, xdpt); 									// Expand any compounds;
//		if(flat.size() == 0)
//			throw new IllegalStateException("Expansion for property " + pmm + " resulted in 0 columns!?");
//
//		/*
//		 * We have an expanded property, either one that exploded into > 1 columns or an expansion that changed the type
//		 * of the column (which happens when the column is converted using a join string conversion). We will create a
//		 * synthetic column which will "contain" all of the real generated columns. Lots of operations are not valid
//		 * on synthetic column definitions because they cannot be "spread" over the individual columns.
//		 */
//		ExpandedColumnDef<V> xcd = new ExpandedColumnDef<V>(this, pmm.getActualType(), pmm.getName());
//		for(final ExpandedDisplayProperty< ? > xdp : flat) {
//			if(xdp.getName() == null)
//				throw new IllegalStateException("All columns MUST have some name");
//			ColumnDef< ? > ccd = addExpandedDisplayProp(xdp);
//			xcd.addExpanded(ccd);
//		}
//		return xcd;
//	}
}
