package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Binds two {@link IObservableValue}'s together, and keeps one up to date with the other.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class BndValue<T> {
	@Nonnull
	private final IObservableValue<T> m_model;

	@Nonnull
	private final IObservableValue<T> m_target;

	private IValueChangeListener<T> m_targetListener;

	private IValueChangeListener<T> m_modelListener;

	public BndValue(@Nonnull IObservableValue<T> model, @Nonnull IObservableValue<T> target) {
		m_model = model;
		m_target = target;

		//-- Add the appropriate listeners
		IValueChangeListener<T> tl = m_targetListener = new IValueChangeListener<T>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<T> event) throws Exception {

			}
		};
		target.addChangeListener(tl);

		IValueChangeListener<T> ml = m_modelListener = new IValueChangeListener<T>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<T> event) throws Exception {

			}
		};
		model.addChangeListener(ml);
	}


}
