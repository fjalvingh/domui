package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

/**
 * Base class for a binding.
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
		} catch(ValidationException vx) {
			return;
		}
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
	}

}
