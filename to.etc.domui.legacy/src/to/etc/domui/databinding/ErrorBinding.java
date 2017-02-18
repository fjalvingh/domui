package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.databinding.value.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

/**
 * A binding to a {@link NodeBase#getMessage} property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2013
 */
public class ErrorBinding implements IValueChangeListener<UIMessage> {
	@Nonnull
	final private BindingContext m_bindingContext;

	@Nullable
	private IObservableValue<UIMessage> m_observable;

	private UIMessage m_message;

	public ErrorBinding(@Nonnull BindingContext bindingContext, @Nonnull IObservableValue<UIMessage> observable) {
		m_bindingContext = bindingContext;
		m_observable = observable;
		observable.addChangeListener(this);
	}

	public ErrorBinding(@Nonnull BindingContext bindingContext, @Nonnull UIMessage error) {
		m_bindingContext = bindingContext;
		m_message = error;
	}

	/**
	 * Called when the control's message property changes. Delegates the change to the context.
	 * @see to.etc.domui.databinding.value.IValueChangeListener#handleChange(to.etc.domui.databinding.value.ValueChangeEvent)
	 */
	@Override
	public void handleChange(@Nonnull ValueChangeEvent<UIMessage> event) throws Exception {
		m_bindingContext.errorBindingChanged(this, event.getDiff().getOld(), event.getDiff().getNew());
	}

	public void dispose() {
		if(null != m_observable) {
			m_observable.removeChangeListener(this);
			m_observable = null;								// Be very sure we're dead.
		}
	}

	public void setMessage(@Nullable UIMessage error) throws Exception {
		if(null != m_observable)
			m_observable.setValue(error);
		m_message = error;
	}

}
