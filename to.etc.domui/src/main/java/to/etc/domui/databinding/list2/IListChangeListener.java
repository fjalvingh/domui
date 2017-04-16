package to.etc.domui.databinding.list2;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.observables.*;



/**
 * Listener for {@link IObservableList} changes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public interface IListChangeListener<T> extends IChangeListener<T, ListChangeEvent<T>, IListChangeListener<T>> {
}
