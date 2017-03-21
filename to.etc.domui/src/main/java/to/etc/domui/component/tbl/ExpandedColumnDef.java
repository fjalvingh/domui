package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

public class ExpandedColumnDef<T> {
	@Nonnull
	final private ColumnDefList< ? > m_defList;

	@Nonnull
	private List<SimpleColumnDef< ? >> m_childColumns = Collections.EMPTY_LIST;

	@Nonnull
	final private Class<T> m_columnType;

	@Nonnull
	final private String m_propertyName;

	public ExpandedColumnDef(@Nonnull ColumnDefList< ? > defList, @Nonnull Class<T> columnType, @Nonnull String propertyName) {
		m_defList = defList;
		m_columnType = columnType;
		m_propertyName = propertyName;
	}

	/**
	 * Return T if this column is expanded but only to one other column.
	 * @return
	 */
	public boolean isSimple() {
		return m_childColumns.size() == 1;
	}

	@Nonnull
	public SimpleColumnDef< ? > simple() {
		if(m_childColumns.size() != 1)
			throw new IllegalStateException("Cannot call this with non-simple expanded column");
		return m_childColumns.get(0);
	}

	@Nonnull
	public String getPropertyName() {
		return m_propertyName;
	}

	/**
	 * When this def actually represents a set of columns, expanded because of the source property being
	 * an expanded property, then this adds all of the source columns.
	 * @param ccd
	 */
	protected void addExpanded(@Nonnull SimpleColumnDef< ? > ccd) {
		if(m_childColumns.size() == 0)
			m_childColumns = new ArrayList<SimpleColumnDef< ? >>();
		m_childColumns.add(ccd);
	}

	@Nullable
	public String getWidth() {
		return simple().getWidth();
	}

	@Nonnull
	public ExpandedColumnDef<T> width(@Nullable String w) {
		simple().width(w);
		return this;
	}

	@Nonnull
	public SortableType getSortable() {
		return simple().getSortable();
	}

	private void setSortable(@Nonnull SortableType sortable) {
		simple().setSortable(sortable);
	}

	/**
	 * Set the default sort order to ascending first.
	 * @return
	 */
	@Nonnull
	public ExpandedColumnDef<T> ascending() {
		setSortable(SortableType.SORTABLE_ASC);
		return this;
	}

	/**
	 * Set the default sort order to descending first.
	 * @return
	 */
	@Nonnull
	public ExpandedColumnDef<T> descending() {
		setSortable(SortableType.SORTABLE_DESC);
		return this;
	}

	/**
	 * Set this column as the default column to sort on.
	 * @return
	 */
	@Nonnull
	public ExpandedColumnDef<T> sortdefault() {
		m_defList.setSortColumn(simple());
		return this;
	}


}
