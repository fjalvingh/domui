package to.etc.domui.component.tbl;

/**
 * A tablemodel that can also change it's backing data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
public interface IModifyableTableModel<T> extends ITableModel<T> {
	void add(int index, T row) throws Exception;

	void add(T row) throws Exception;

	T delete(int index) throws Exception;

	boolean delete(T val) throws Exception;
}
