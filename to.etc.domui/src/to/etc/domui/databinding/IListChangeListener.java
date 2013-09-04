package to.etc.domui.databinding;


/**
 * Listener for {@link IObservableList} changes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public interface IListChangeListener<T> extends IChangeListener<T, ListChangeEvent<T>, IListChangeListener<T>> {
}
