package to.etc.domui.component.tbl;

/**
 * Accepts datatable change events.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public interface IDataTableChangeListener {
	/**
	 * Called when the datatable as accepted a different model.
	 */
	public void		modelChanged(TabularComponentBase tbl, ITableModel<?> old, ITableModel<?> nw) throws Exception;

	public void		pageChanged(TabularComponentBase tbl) throws Exception;
}
