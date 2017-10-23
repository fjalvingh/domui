package to.etc.domui.component.tbl;

import kotlin.reflect.KProperty1;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.annotations.GProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

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

	/** The factor to multiply the #of characters with to get the real em width of a column. */
	private double m_emFactor = 0.65;

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

	public Stream<ColumnDef<T, ?>> stream() {
		return m_columnList.stream();
	}

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

	public double getEmFactor() {
		return m_emFactor;
	}

	public void setEmFactor(double emFactor) {
		m_emFactor = emFactor;
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
