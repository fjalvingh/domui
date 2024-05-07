package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A model that stores selections for a table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public interface ISelectionModel<T> {
	/**
	 * T if this model can handle multiple selections.
	 */
	boolean isMultiSelect();

	/**
	 * Return T if this instance is actually selected.
	 */
	boolean isSelected(@NonNull T rowinstance);

	/**
	 * Return the #of instances currently selected.
	 */
	int getSelectionCount();

	/**
	 * Set or clear an instance's selected state. When changed the model will call the listeners
	 * to report the change.
	 */
	void setInstanceSelected(@NonNull T rowinstance, boolean on) throws Exception;

	/**
	 * Clear all selections, then call the listeners to report the change.
	 */
	void clearSelection() throws Exception;

	/**
	 * Clear all values selected from the specified model.
	 */
	void clearSelection(ITableModel<T> model) throws Exception;

	/**
	 * This must add all (recoverable) items in the model and add them as selected as efficiently
	 * as possible. It is NEVER called directly, but always through a {@link ISelectionAllHandler},
	 * so that "select all" can be forbidden.
	 */
	void selectAll(@NonNull ITableModel<T> in) throws Exception;

	/**
	 * Add a listener that will receive notifications when the selections change.
	 */
	void addListener(@NonNull ISelectionListener<T> l);

	void removeListener(@NonNull ISelectionListener<T> l);

	/**
	 * This returns T if all items in the table model passed
	 * are present in the selection model. We need to have this
	 * to be able to handle lazy models.
	 */
	boolean isCompleteModelSelected(ITableModel<T> model) throws Exception;
}
