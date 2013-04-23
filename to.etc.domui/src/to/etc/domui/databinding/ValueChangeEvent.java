package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * An event due to a change in some observable value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ValueChangeEvent<T> extends ObservableEvent<T, ValueChangeEvent<T>, IValueChangeListener<T>> {
	@Nonnull
	final private ValueDiff<T> m_diff;

	public ValueChangeEvent(@Nonnull IObservableValue<T> source, @Nonnull ValueDiff<T> diff) {
		super(source);
		m_diff = diff;
	}

	/**
	 * The observable that this event sprung from.
	 * @return
	 */
	@Override
	@Nonnull
	public IObservableValue<T> getSource() {
		return (IObservableValue<T>) super.getSource();
	}

	@Nonnull
	public ValueDiff<T> getDiff() {
		return m_diff;
	}
}
