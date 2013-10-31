package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.util.*;

/**
 * An observable property that contains an observable list as it's contents.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 31, 2013
 */
public interface IObservableListValue<E> extends IObservable<List<E>, ListChangeEvent<E>, IListChangeListener<E>>, IReadWriteModel<List<E>> {
	@Nonnull
	public Class<T> getValueType();

	/**
	 * Return the current value of the observable.
	 * @return
	 */
	@Nullable
	@Override
	public T getValue() throws Exception;

	@Override
	public void setValue(@Nullable T value) throws Exception;
}
