package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.util.*;

public interface IObservableValue<T> extends IObservable<T, ValueChangeEvent<T>, IValueChangeListener<T>>, IReadWriteModel<T> {
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
