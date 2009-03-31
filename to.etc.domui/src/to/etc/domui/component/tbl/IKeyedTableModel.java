package to.etc.domui.component.tbl;

public interface IKeyedTableModel<T> extends ITableModel<T> {
	public String	getRowKey(int row) throws Exception;

	public T		findRowObject(String key) throws Exception;
}
