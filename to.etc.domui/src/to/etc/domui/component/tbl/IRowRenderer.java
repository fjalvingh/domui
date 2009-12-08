package to.etc.domui.component.tbl;

/**
 * Delegate for a table which must render a row of items from a single row object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public interface IRowRenderer<T> {
	abstract public void beforeQuery(TableModelTableBase<T> tbl) throws Exception;

	abstract public void renderRow(TableModelTableBase<T> tbl, ColumnContainer<T> cc, int index, T instance) throws Exception;

	abstract public void renderHeader(TableModelTableBase<T> tbl, HeaderContainer<T> cc) throws Exception;
}
