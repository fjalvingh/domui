package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

/**
 * Base class for a binding. Also contains one side of the listener data (source -> target) because that is always there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 22, 2013
 */
public class Binding {
	@Nonnull
	final private BindingContext m_context;

	@Nonnull
	final protected IObservableValue< ? > m_from;

	@Nullable
	protected IObservableValue< ? > m_to;

	@Nullable
	protected IReadWriteModel< ? > m_tomodel;

	@Nullable
	private IValueChangeListener< ? > m_fromListener;

	/** If not null, the IUniConverter OR IJoinConverter which converts values for this binding. */
	@Nullable
	protected IUniConverter< ? , ? > m_converter;

	@Nullable
	private UIMessage m_message;

	protected Binding(@Nonnull BindingContext context, @Nonnull IObservableValue< ? > sourceo) {
		m_from = sourceo;
		m_context = context;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Internal.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a listener on FROM, so that changes there propagate to TO.
	 * @param value
	 */
	protected <V> void addSourceListener() {
		IValueChangeListener<V> ml = new IValueChangeListener<V>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<V> event) throws Exception {
				moveSourceToTarget();
			}
		};
		m_fromListener = ml;
		((IObservableValue<V>) m_from).addChangeListener(ml);
	}

	/**
	 * Internal: move source to target using an optional conversion.
	 * @throws Exception
	 */
	protected void moveSourceToTarget() throws Exception {
		Object val;
		try {
			val = ((IObservableValue<Object>) m_from).getValue();
			IUniConverter<Object, Object> uc = (IUniConverter<Object, Object>) m_converter;
			if(null != uc) {
				val = uc.convertSourceToTarget(val);
			}
			if(m_to != null)
				((IObservableValue<Object>) m_to).setValue(val);
			else if(m_tomodel != null)
				((IReadWriteModel<Object>) m_tomodel).setValue(val);
			else
				throw new IllegalStateException("Neither to nor tomodel is set");
		} catch(ValidationException vx) {
			bindingError(vx);
			return;
		}
	}

	private void bindingError(@Nullable ValidationException vx) {
		UIMessage old = m_message;

		if(vx == null) {
			if(old == null)
				return;
			m_message = null;
			m_context.bindingErrorChanged(this, old, null);
		} else {
			UIMessage nw = UIMessage.error(vx);						// Convert exception to message
			if(nw.equals(old))										// Same message -> no change
				return;
			m_message = nw;
			m_context.bindingErrorChanged(this, old, nw);
		}
	}

}
