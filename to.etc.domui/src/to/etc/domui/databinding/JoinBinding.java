package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
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

	JoinBinding(@Nonnull BindingContext context, @Nonnull IObservableValue< ? > sourceo) {
		super(context, sourceo);
	}

	/**
	 * Belongs to a {@link Bind#from(Object, String)} call and defines the target side of
	 * an unidirectional binding. Changes in "from" move to "to", but changes in "to" do
	 * not move back.
	 *
	 * @param target
	 * @param property
	 * @return
	 */
	@Nonnull
	public <T, V, X, Y> JoinBinding to(@Nonnull T target, @Nonnull String property, @Nullable IJoinConverter<X, Y> convert) throws Exception {
		if(null == target || null == property)
			throw new IllegalArgumentException("target/property cannot be null");
		m_converter = convert;
		IObservableValue<V> targeto = (IObservableValue<V>) BindingContext.createObservable(target, property);
		m_to = targeto;
		addSourceListener();
		addTargetListener();

		//-- Immediately move the value of source to target too 2
		moveSourceToTarget();
		return this;
	}

	public <T, V> JoinBinding to(@Nonnull T target, @Nonnull String property) throws Exception {
		return to(target, property, null);
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

			@Override
			public void setError(UIMessage error) {
				setBindingError(error);
			}
		};
		m_toListener = ml;
		IObservableValue< ? > to = m_to;
		if(null == to)
			throw new IllegalStateException("'to' has not been defined");
		((IObservableValue<V>) to).addChangeListener(ml);
	}

	protected void moveTargetToSource() throws Exception {
		Object val;
		try {
			IObservableValue< ? > to = m_to;
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