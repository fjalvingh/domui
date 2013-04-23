package to.etc.domui.databinding;

import javax.annotation.*;

public interface IObservableValue<T, E extends IChangeEvent<T, E, L>, L extends IChangeListener<T, E, L>> extends IObservable<T, E, L> {
	@Nonnull
	public Class<T> getValueType();

	/**
	 * Return the current value of the observable.
	 * @return
	 */
	@Nullable
	public T getValue() throws Exception;

	public void setValue(@Nullable T value) throws Exception;
}
