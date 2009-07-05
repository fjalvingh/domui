package to.etc.domui.component.tbl;

/**
 * Delegate for a table which must render a row of items from a single row object.
 * FIXME Needs to be generic
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface IRowRenderer {
	abstract public void beforeQuery(DataTable tbl) throws Exception;

	abstract public void renderRow(DataTable tbl, ColumnContainer cc, int index, Object instance) throws Exception;

	abstract public void renderHeader(DataTable tbl, HeaderContainer cc) throws Exception;
}
