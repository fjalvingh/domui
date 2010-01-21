package to.etc.domui.component.tbl;

import java.util.*;

/**
 * The model for a table. This is the abstract type.
 * A table model contains a list of objects accessible by index and by
 * key. Access by index is used to handle paging.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface ITableModel<T> {
	/**
	 * Default size used to truncate results in case of large datasets as query results, if no other limit has been set.
	 */
	public static final int DEFAULT_MAX_SIZE = 1000;

	public List<T> getItems(int start, int end) throws Exception;

	/**
	 * This must return the total #of rows in this table.
	 * @return
	 */
	public int getRows() throws Exception;

	public void addChangeListener(ITableModelListener<T> l);

	public void removeChangeListener(ITableModelListener<T> l);
}
