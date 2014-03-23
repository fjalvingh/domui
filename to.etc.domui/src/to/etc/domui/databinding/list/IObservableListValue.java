package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.util.*;

/**
 * An observable property that contains an observable list as it's contents.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 31, 2013
 */
public interface IObservableListValue<E> extends IObservable<List<E>, ListValueChangeEvent<E>, IListValueChangeListener<E>>, IReadWriteModel<List<E>> {
	/**
	 * Return the current value of the observable.
	 * @return
	 */
	@Nullable
	@Override
	public List<E> getValue() throws Exception;

	@Override
	public void setValue(@Nullable List<E> value) throws Exception;
}
