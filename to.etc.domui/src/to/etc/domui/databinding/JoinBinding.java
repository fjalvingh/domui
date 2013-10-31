package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.databinding.value.*;
import to.etc.domui.trouble.*;

/**
 * Bidirectional binding a &lt;--&gt; b.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 30, 2013
 */
public class JoinBinding extends Binding {
	@Nullable
	private IValueChangeListener< ? > m_toListener;

	@Nonnull
	private IObservableValue< ? > m_target;

	JoinBinding(@Nonnull BindingContext context, @Nonnull IObservableValue< ? > sourceo, @Nonnull IObservableValue< ? > targeto) throws Exception {
		super(context, sourceo);
		m_target = targeto;
		addSourceListener();
		addTargetListener();
		moveSourceToTarget();
	}

	final public <A, B> JoinBinding converter(@Nonnull IJoinConverter<A, B> jc) {
		m_converter = jc;
		return this;
	}

	@Override
	protected void moveSourceToTarget() throws Exception {
		Object val;
		try {
			val = ((IObservableValue<Object>) m_from).getValue();
			IUniConverter<Object, Object> uc = (IUniConverter<Object, Object>) m_converter;
			if(null != uc) {
				val = uc.convertSourceToTarget(val);
			}
			((IObservableValue<Object>) m_target).setValue(val);
		} catch(ValidationException vx) {
			bindingError(vx);
			return;
		}
	}

	/**
	 * Add a listener on TO, so that changes there propagate back to FROM.
	 * @param value
	 */
	protected <V> void addTargetListener() {
		IValueChangeListener<V> ml = new IValueChangeListener<V>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<V> event) throws Exception {
				moveTargetToSource();
			}
		};
		m_toListener = ml;
		IObservableValue< ? > to = m_target;
		if(null == to)
			throw new IllegalStateException("'to' has not been defined");
		((IObservableValue<V>) to).addChangeListener(ml);
	}

	protected void moveTargetToSource() throws Exception {
		Object val;
		try {
			IObservableValue< ? > to = m_target;
			if(null == to)
				throw new IllegalStateException("'to' has not been defined");
			val = ((IObservableValue<Object>) to).getValue();
		} catch(ValidationException vx) {
			return;
		}

		IJoinConverter<Object, Object> uc = (IJoinConverter<Object, Object>) m_converter;
		if(null != uc) {
			val = uc.convertTargetToSource(val);
		}
		((IObservableValue<Object>) m_from).setValue(val);
	}
}