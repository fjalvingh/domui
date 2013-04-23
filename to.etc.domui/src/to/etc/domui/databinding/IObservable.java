package to.etc.domui.databinding;

import javax.annotation.*;

public interface IObservable<T, E extends IChangeEvent<T, E, L>, L extends IChangeListener<T, E, L>> {
	/**
	 * Adds a change listener for this observable item. The listener is notified of changes on the specified observable.
	 * @param listener
	 */
	public void addChangeListener(@Nonnull L listener);

	/**
	 * Remove the specified listener for changes on this observable item.
	 * @param listener
	 */
	public void removeChangeListener(@Nonnull L listener);
}
