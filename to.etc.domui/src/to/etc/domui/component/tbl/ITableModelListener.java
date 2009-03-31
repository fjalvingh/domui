package to.etc.domui.component.tbl;

/**
 * Thingy which receives events from a table model. When a model changes it
 * must pass the changes on to it's listeners. The DataTable component for
 * instance registers itself as a listener to it's attached model. It uses
 * the events to re-draw the parts of the table that have changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 6, 2008
 */
public interface ITableModelListener<T> {
	/**
	 * Called after a row is added to the model.
	 *
	 * @param model
	 * @param index
	 * @param value
	 * @throws Exception
	 */
	public void			rowAdded(ITableModel<T> model, int index, T value) throws Exception;

	/**
	 * Called after a row has been deleted.
	 *
	 * @param model
	 * @param index	The index of the deleted row in the table.
	 * @param value	The <b>deleted</b> value.
	 * @throws Exception
	 */
	public void			rowDeleted(ITableModel<T> model, int index, T value) throws Exception;

	/**
	 * Called after a row has been changed.
	 * @param model
	 * @param index
	 * @param value
	 * @throws Exception
	 */
	public void			rowModified(ITableModel<T> model, int index, T value) throws Exception;

	/**
	 * Called when the entire content of the model changed. This should indicate a complete content
	 * redraw usually.
	 * @param model
	 */
	public void			modelChanged(ITableModel<T> model);
}
