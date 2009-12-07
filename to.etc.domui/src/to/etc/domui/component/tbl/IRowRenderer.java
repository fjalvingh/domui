package to.etc.domui.component.tbl;

/**
 * Delegate for a table which must render a row of items from a single row object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface IRowRenderer<T> {
	abstract public void beforeQuery(DataTable<T> tbl) throws Exception;

	abstract public void renderRow(DataTable<T> tbl, ColumnContainer<T> cc, int index, T instance) throws Exception;

	abstract public void renderHeader(DataTable<T> tbl, HeaderContainer<T> cc) throws Exception;
}
