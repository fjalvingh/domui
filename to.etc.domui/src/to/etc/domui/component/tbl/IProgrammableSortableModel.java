package to.etc.domui.component.tbl;

public interface IProgrammableSortableModel extends ISortableTableModel {
	void sortOn(ISortHelper helper, boolean descending) throws Exception;
}
