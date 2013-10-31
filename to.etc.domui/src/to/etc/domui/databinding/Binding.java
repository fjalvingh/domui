package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.databinding.value.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;

/**
 * Base class for a binding. Also contains one side of the listener data (source -> target) because that is always there.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 22, 2013
 */
abstract public class Binding {
	@Nonnull
	final private BindingContext m_context;

	@Nonnull
	final protected IObservableValue< ? > m_from;

	@Nullable
	private IValueChangeListener< ? > m_fromListener;

	/** If not null, the IUniConverter OR IJoinConverter which converts values for this binding. */
	@Nullable
	protected IUniConverter< ? , ? > m_converter;

	/**
	 * Internal: move source to target using an optional conversion.
	 * @throws Exception
	 */
	abstract protected void moveSourceToTarget() throws Exception;


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

	protected void bindingError(@Nullable ValidationException vx) {
		m_context.bindingError(this, vx == null ? null : UIMessage.error(vx));
	}
}
