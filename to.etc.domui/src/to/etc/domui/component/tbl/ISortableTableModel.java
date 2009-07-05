package to.etc.domui.component.tbl;

/**
 * When implemented by a TableModel, methods herein will be called when
 * sorting is required.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public interface ISortableTableModel {
	public void sortOn(String key, boolean descending) throws Exception;

	/**
	 * If this model is currently sorted, this returns an identifier (usually a
	 * property reference) indicating on which column the thingy is sorted. If
	 * the model is unsorted this returns null.
	 * @return
	 */
	public String getSortKey();

	/**
	 * If the set is a sorted set, this returns TRUE if the sort order is 
	 * descending. The return value is <b>undefined</b> for an unsorted model.
	 * @return
	 */
	public boolean isSortDescending();
}
