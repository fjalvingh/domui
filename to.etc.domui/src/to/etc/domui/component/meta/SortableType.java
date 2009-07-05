package to.etc.domui.component.meta;

public enum SortableType {
	UNKNOWN, UNSORTABLE, SORTABLE_ASC, SORTABLE_DESC;

	public boolean isSortable() {
		return this == SORTABLE_ASC || this == SORTABLE_DESC;
	}
}
