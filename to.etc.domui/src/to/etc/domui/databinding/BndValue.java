package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Binds two {@link IObservableValue}'s together.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class BndValue<T> {
	@Nonnull
	private final IObservableValue<T> m_model;

	@Nonnull
	private final IObservableValue<T> m_target;

	public BndValue(@Nonnull IObservableValue<T> model, @Nonnull IObservableValue<T> target) {
		m_model = model;
		m_target = target;
	}



}
