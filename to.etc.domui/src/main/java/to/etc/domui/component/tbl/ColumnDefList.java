package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list of {@link SimpleColumnDef} columns used to define characteristics of columns in any
 * tabular presentation. This class maintains the list, and has utility methods to manipulate
 * that list.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 11, 2012
 */
final public class ColumnDefList<T> implements Iterable<SimpleColumnDef<?>> {
	//static private final Logger LOG = LoggerFactory.getLogger(ColumnDefList.class);

	static public final String NUMERIC_CSS_CLASS = "ui-numeric";

	@NonNull
	final private ClassMetaModel m_metaModel;

	@NonNull
	final private List<SimpleColumnDef<?>> m_columnList = new ArrayList<>();

	@Nullable
	private SimpleColumnDef<?> m_sortColumn;

	@NonNull
	final private Class<T> m_rootClass;

	public ColumnDefList(@NonNull Class<T> rootClass, @NonNull ClassMetaModel cmm) {
		m_rootClass = rootClass;
		m_metaModel = cmm;
//		m_sortDescending = cmm.getDefaultSortDirection() == SortableType.SORTABLE_DESC;
	}

	public int size() {
		return m_columnList.size();
	}

	public void add(@NonNull SimpleColumnDef<?> cd) {
		m_columnList.add(cd);
	}

	@NonNull
	private ClassMetaModel model() {
		return m_metaModel;
	}

	@NonNull
	public SimpleColumnDef<?> get(int ix) {
		if(ix < 0 || ix >= m_columnList.size())
			throw new IndexOutOfBoundsException("Column " + ix + " does not exist");
		return m_columnList.get(ix);
	}

	@Nullable
	public SimpleColumnDef<?> findColumn(@NonNull String propertyName) {
		for(final SimpleColumnDef<?> scd : m_columnList) {
			if(DomUtil.isEqual(scd.getPropertyName(), propertyName)) {
				return scd;
			}
		}
		return null;
	}

	/**
	 * Set the default sort column by property name. If it is null the default sort is undone.
	 */
	public void setDefaultSortColumn(@Nullable String sort) {
		if(null == sort) {
			m_sortColumn = null;
		} else {
			SimpleColumnDef<?> scd = findColumn(sort);
			if(null != scd)
				setSortColumn(scd);
		}
	}

	public void setSortColumn(@Nullable SimpleColumnDef<?> cd) {
		m_sortColumn = cd;
	}

	public void addColumn(String property) {
		//-- Property must refer a property, so get it;
		final PropertyMetaModel<?> pmm = m_metaModel.findProperty(property);
		if(pmm == null)
			throw new IllegalArgumentException("Undefined property path: '" + property + "' in classModel=" + m_metaModel);

		//-- This is a property to display. Expand it into DisplayProperties to get the #of columns to append.
		final ExpandedDisplayProperty<?> xdpt = ExpandedDisplayProperty.expandProperty(pmm);
		final List<ExpandedDisplayProperty<?>> flat = new ArrayList<>();
		ExpandedDisplayProperty.flatten(flat, xdpt); // Expand any compounds;

		//-- And finally: add all columns ;-)
		for(final ExpandedDisplayProperty<?> xdp : flat) {
			SimpleColumnDef<?> scd = new SimpleColumnDef<>(this, xdp);
			add(scd);
			scd.setDisplayLength(xdp.getDisplayLength());
		}
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		final List<DisplayPropertyMetaModel> dpl = m_metaModel.getTableDisplayProperties();
		if(dpl.isEmpty())
			throw new IllegalStateException(
				"The list-of-columns to show is empty, and the class " + m_metaModel.getActualClass() + " has no @MetaObject definition defining a set of columns as default table columns, so there.");
		List<ExpandedDisplayProperty<?>> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, m_metaModel, null);
		xdpl = ExpandedDisplayProperty.flatten(xdpl); // Flatten the list: expand any compounds.
		for(final ExpandedDisplayProperty<?> xdp : xdpl) {
			addExpandedDisplayProp(xdp);
		}
	}

	@NonNull
	private <V> SimpleColumnDef<V> addExpandedDisplayProp(@NonNull ExpandedDisplayProperty<V> xdp) {
		SimpleColumnDef<V> scd = new SimpleColumnDef<>(this, xdp);
		if(scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
			scd.setCssClass(NUMERIC_CSS_CLASS);
			scd.setHeaderCssClass(NUMERIC_CSS_CLASS);
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
		//-- Loop 1: calculate current size allocations for columns that have a width assigned.
		int totpct = 0;
		int totpix = 0;
		int ntoass = 0; // #columns that need a width
		int totdw = 0; // Total display width of all unassigned columns.
		for(final SimpleColumnDef<?> scd : m_columnList) {
			String cwidth = scd.getWidth();
			if(cwidth == null || cwidth.isEmpty()) {
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
			for(final SimpleColumnDef<?> scd : m_columnList) {
				String width = scd.getWidth();
				if(width == null || width.isEmpty()) {
					//-- Calculate a size factor, then use it to assign
					final double fact = (double) scd.getDisplayLength() / totdw;
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
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@NonNull
	public Iterator<SimpleColumnDef<?>> iterator() {
		return m_columnList.iterator();
	}

	public int indexOf(@NonNull SimpleColumnDef<?> scd) {
		return m_columnList.indexOf(scd);
	}

	@Nullable
	public SimpleColumnDef<?> getSortColumn() {
		return m_sortColumn;
	}

//	protected void updateDefaultSort(@NonNull SimpleColumnDef< ? > scd) {
//		if(m_sortColumn == scd)
//			m_sortDescending = scd.getSortable() == SortableType.SORTABLE_DESC;
//	}
//
//	public boolean isSortDescending() {
//		return m_sortDescending;
//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Typeful column definition code.						*/
	/*--------------------------------------------------------------*/

	/**
	 * Add and return the column definition for a column on the specified property. Because Java still has no
	 * first-class properties (sigh) you need to pass in the property's type to get a typeful column. If you
	 * do not need a typeful column use {@link #column(String)}.
	 */
	@NonNull
	public <V> SimpleColumnDef<V> column(@NonNull Class<V> type, @NonNull String property) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) model().getProperty(property);
		return createColumnDef(pmm);
	}

	@NonNull
	private <V> SimpleColumnDef<V> createColumnDef(@NonNull PropertyMetaModel<V> pmm) {
		SimpleColumnDef<V> scd = new SimpleColumnDef<>(this, pmm);
		scd.setNowrap(Boolean.TRUE);
		add(scd);
		return scd;
	}

	/**
	 * This adds a column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 */
	@NonNull
	public SimpleColumnDef<?> column(@NonNull String property) {
		PropertyMetaModel<?> pmm = model().getProperty(property);            // Get the appropriate model
		return createColumnDef(pmm);
	}

	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 */
	@NonNull
	public SimpleColumnDef<T> column() {
		SimpleColumnDef<T> scd = new SimpleColumnDef<>(this, m_rootClass);
		add(scd);
		scd.setNowrap(Boolean.TRUE);
		return scd;
	}

	/**
	 *
	 */
	@NonNull
	public <V> ExpandedColumnDef<V> expand(@NonNull Class<V> clz, @NonNull String property) {
		PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) model().getProperty(property);
		return createExpandedColumnDef(pmm);
	}

	/**
	 * This adds an expanded column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 */
	@NonNull
	public ExpandedColumnDef<?> expand(@NonNull String property) {
		PropertyMetaModel<?> pmm = model().getProperty(property);            // Get the appropriate model
		return createExpandedColumnDef(pmm);
	}

	/**
	 * This gets called when the property is to be expanded.
	 */
	@NonNull
	private <V> ExpandedColumnDef<V> createExpandedColumnDef(@NonNull PropertyMetaModel<V> pmm) {
		//-- Try to see what the column expands to
		final ExpandedDisplayProperty<?> xdpt = ExpandedDisplayProperty.expandProperty(pmm);
		final List<ExpandedDisplayProperty<?>> flat = new ArrayList<>();
		ExpandedDisplayProperty.flatten(flat, xdpt);                                    // Expand any compounds;
		if(flat.isEmpty())
			throw new IllegalStateException("Expansion for property " + pmm + " resulted in 0 columns!?");

		/*
		 * We have an expanded property, either one that exploded into > 1 columns or an expansion that changed the type
		 * of the column (which happens when the column is converted using a join string conversion). We will create a
		 * synthetic column which will "contain" all of the real generated columns. Lots of operations are not valid
		 * on synthetic column definitions because they cannot be "spread" over the individual columns.
		 */
		ExpandedColumnDef<V> xcd = new ExpandedColumnDef<>(this, pmm.getActualType(), pmm.getName());
		for(final ExpandedDisplayProperty<?> xdp : flat) {
			SimpleColumnDef<?> ccd = addExpandedDisplayProp(xdp);
			xcd.addExpanded(ccd);
		}
		return xcd;
	}

}
