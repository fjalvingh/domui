package to.etc.domui.databinding;

import javax.annotation.*;

public interface IObservableValue<T> extends IObservable<T, ValueChangeEvent<T>, IValueChangeListener<T>> {
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
