package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.annotations.*;

/**
 * A list of columns defined in a new-style row renderer.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2014
 */
public class ColumnList<T> implements Iterable<ColumnDef< ? >> {
	@Nonnull
	final private ClassMetaModel m_metaModel;

	@Nonnull
	final private List<ColumnDef< ? >> m_columnList = new ArrayList<ColumnDef< ? >>();

	@Nullable
	private ColumnDef< ? > m_sortColumn;

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

	public void add(@Nonnull ColumnDef< ? > cd) {
		if(null == cd)
			throw new IllegalArgumentException("Cannot be null");
		m_columnList.add(cd);
	}

	@Nonnull
	private ClassMetaModel model() {
		return m_metaModel;
	}

	@Nonnull
	public ColumnDef< ? > get(int ix) {
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
			for(final ColumnDef< ? > scd : m_columnList) {
				if(DomUtil.isEqual(scd.getPropertyName(), sort)) {
					setSortColumn(scd, scd.getSortable());
					break;
				}
			}
		}
	}

	public void setSortColumn(@Nullable ColumnDef< ? > cd, @Nullable SortableType type) {
		m_sortColumn = cd;
		m_sortDescending = type == SortableType.SORTABLE_DESC;
	}

	public void setSortColumn(@Nullable ColumnDef< ? > cd) {
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
	private <V> ColumnDef<V> addExpandedDisplayProp(@Nonnull ExpandedDisplayProperty<V> xdp) {
		ColumnDef<V> scd = new ColumnDef<V>(this, xdp);
		if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
			scd.setCssClass("ui-numeric");
			scd.setHeaderCssClass("ui-numeric");
		}

		m_columnList.add(scd);
		return scd;
	}

	/**
	 * Width calculations: this tries to assign widths to columns that have no explicit width assigned. It starts
	 * by calculating all assigned widths in percents and in pixels. It then calculates widths for the columns that
	 * have no widths assigned.
	 */
	public void assignPercentages() {
		/*
		 */
		//-- Loop 1: calculate current size allocations for columns that have a width assigned.
		int totpct = 0;
		int totpix = 0;
		int ntoass = 0; // #columns that need a width
		int totdw = 0; // Total display width of all unassigned columns.
		for(final ColumnDef< ? > scd : m_columnList) {
			String cwidth = scd.getWidth();
			if(cwidth == null || cwidth.length() == 0) {
				ntoass++;
				totdw += scd.getDisplayLength();
			} else {
				final String s = cwidth.trim();
				if(s.endsWith("%")) {
					final int w = StringTool.strToInt(s.substring(0, s.length() - 1).trim(), -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width percentage: " + s + " for presentation column " + scd.getPropertyName());
					totpct += w;
				} else {
					//-- Should be numeric width, in pixels,
					final int w = StringTool.strToInt(s, -1);
					if(w == -1)
						throw new IllegalArgumentException("Invalid width #pixels: " + s + " for presentation column " + scd.getPropertyName());
					totpix += w;
				}
			}
		}

		//-- Is there something to assign, and are the numbers reasonable? If so calculate...
		final int pixwidth = 1280;
		if(ntoass > 0 && totpct < 100 && totpix < pixwidth) {
			int pctleft = 100 - totpct; // How many percents left?
			if(pctleft == 100 && totpix > 0) {
				//-- All widths assigned in pixels... Calculate a percentage of the #pixels left
				pctleft = (100 * (pixwidth - totpix)) / pixwidth;
			}

			//-- Reassign the percentage left over all unassigned columns. Do it streaming, to ensure we reach 100%
			for(final ColumnDef< ? > scd : m_columnList) {
				String width = scd.getWidth();
				if(width == null || width.length() == 0) {
					//-- Calculate a size factor, then use it to assign
					final double fact = (double) scd.getDisplayLength() / (double) totdw;
					final int pct = (int) (fact * pctleft + 0.5);
					pctleft -= pct;
					totdw -= scd.getDisplayLength();
					scd.setWidth(pct + "%");
				}
			}
		}
	}

	/**
	 * Return the iterator for all elements.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@Nonnull
	public Iterator<ColumnDef< ? >> iterator() {
		return m_columnList.iterator();
	}

	public int indexOf(@Nonnull ColumnDef< ? > scd) {
		return m_columnList.indexOf(scd);
	}

	@Nullable
	public ColumnDef< ? > getSortColumn() {
		return m_sortColumn;
	}

	protected void updateDefaultSort(@Nonnull ColumnDef< ? > scd) {
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
	public <V> ColumnDef<V> column(@Nonnull Class<V> type, @Nonnull @GProperty String property) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) model().getProperty(property);
		return createColumnDef(pmm);
	}

	@Nonnull
	private <V> ColumnDef<V> createColumnDef(@Nonnull PropertyMetaModel<V> pmm) {
		ColumnDef<V> scd = new ColumnDef<V>(this, pmm);
		scd.setNowrap(true);
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
	public ColumnDef< ? > column(@Nonnull @GProperty String property) {
		PropertyMetaModel< ? > pmm = model().getProperty(property);			// Get the appropriate model
		return createColumnDef(pmm);
	}

	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> column() {
		ColumnDef<T> scd = new ColumnDef<T>(this, m_actualClass);
		add(scd);
		scd.setNowrap(true);
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
