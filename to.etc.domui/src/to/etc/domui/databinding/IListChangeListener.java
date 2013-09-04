package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Listener for {@link IObservableList} changes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public interface IListChangeListener<T> extends IChangeListener<T, ListChangeEvent<T>, IListChangeListener<T>> {
	/**
	 * When the list has changed this gets called, with the details on the exact change.
	 * @param event
	 * @throws Exception
	 */
	public void listChanged(@Nonnull ListChangeEvent<T> event) throws Exception;
}
