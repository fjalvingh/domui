package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * A model that stores selections for a table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public interface ISelectionModel<T> {
	/**
	 * T if this model can handle multiple selections.
	 * @return
	 */
	boolean isMultiSelect();

	/**
	 * Return T if this instance is actually selected.
	 * @param rowinstance
	 * @return
	 */
	boolean isSelected(@Nonnull T rowinstance);

	/**
	 * Return the #of instances currently selected.
	 * @return
	 */
	int getSelectionCount();

	/**
	 * Set or clear an instance's selected state. When changed the model will call the listeners
	 * to report the change.
	 * @param rowinstance
	 * @param on
	 * @throws Exception
	 */
	void setInstanceSelected(@Nonnull T rowinstance, boolean on) throws Exception;

	/**
	 * Clear all selections, then call the listeners to report the change.
	 * @throws Exception
	 */
	void clearSelection() throws Exception;

	/**
	 * This must add all (recoverable) items in the model and add them as selected as efficiently
	 * as possible. It is NEVER called directly, but always through a {@link ISelectionAllHandler},
	 * so that "select all" can be forbidden.
	 * @param in
	 * @throws Exception
	 */
	void selectAll(ITableModel<T> in) throws Exception;

	/**
	 * Add a listener that will receive notifications when the selections change.
	 * @param l
	 */
	void addListener(@Nonnull ISelectionListener<T> l);

	void removeListener(@Nonnull ISelectionListener<T> l);
}
