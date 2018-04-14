package to.etc.domui.databinding;

import java.util.*;

import org.eclipse.jdt.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.databinding.observables.*;
import to.etc.domui.databinding.value.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Maintains all bindings, and their validation/error status.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2013
 */
public class BindingContext {
	final private Set<Binding> m_bindingsInErrorSet = new HashSet<Binding>();

	/** Maps POJO instances to a map of bindings on it's properties. */
	final private Map<Object, Map<String, Object>> m_instanceBindingMap = new HashMap<Object, Map<String, Object>>();

	/** Map [instance, property] to an observable which accepts errors. */
	final private Map<Object, Map<String, ErrorBinding>> m_errorBindingMap = new HashMap<Object, Map<String, ErrorBinding>>();


	/**
	 * Create a binding between unnamed entities.
	 * @param source
	 * @param to
	 * @return
	 */
	public <T> Binding join(@NonNull IObservableValue<T> source, @NonNull IObservableValue<T> to) {

		throw new IllegalStateException();
	}

	void registerBinding(@NonNull Binding binding) {

	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Utility methods.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Listen binding.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Apr 30, 2013
	 */
	static public final class Listener {
		private IObservableValue< ? >[] m_observables;

		private int m_recurse;

		public Listener(@NonNull IObservableValue< ? >[] obsar) {
			m_observables = obsar;
		}

		@NonNull
		public Listener call(@NonNull final IBindingListener lsnr) {
			IValueChangeListener<Object> ovs = new IValueChangeListener<Object>() {
				@Override
				public void handleChange(@NonNull ValueChangeEvent<Object> event) throws Exception {
					if(m_recurse > 0)
						return;
					try {
						m_recurse++;
						lsnr.valuesChanged();
					} finally {
						m_recurse--;
					}
				}
			};
			for(IObservableValue< ? > ov : m_observables) {
				IObservableValue<Object> ovo = (IObservableValue<Object>) ov;
				ovo.addChangeListener(ovs);
			}
			return this;
		}
	}

	/**
	 * Start for an "unidirectional" binding: Bind.from().to();
	 * @param source
	 * @param property
	 * @return
	 */
	@NonNull
	public <S, T, V> UnidirectionalBinding unibind(@NonNull S source, @NonNull String sproperty, @NonNull final T target, @NonNull final String tproperty) throws Exception {
		if(null == source || null == sproperty)
			throw new IllegalArgumentException("source/property cannot be null");
		if(null == target || null == tproperty)
			throw new IllegalArgumentException("target/property cannot be null");
		IObservableValue< ? > sourceo = createObservable(source, sproperty);

		//-- Unidirectional binds only need to have access to a setter, we do not need an Observable.
		final PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) MetaManager.getPropertyMeta(target.getClass(), tproperty);
		IReadWriteModel<V> mdl = new IReadWriteModel<V>() {
			@Override
			public V getValue() throws Exception {
				throw new IllegalStateException("Unexpected 'get' in unidirectional binding");
			}

			@Override
			public void setValue(V value) throws Exception {
				pmm.setValue(target, value);
			}
		};

		//-- If target is a NodeBase then bind an error listener too
		if(target instanceof NodeBase) {
			NodeBase nb = (NodeBase) target;
			bindMessage(source, sproperty, nb);
		}

		UnidirectionalBinding udb = new UnidirectionalBinding(this, sourceo, mdl);
		return udb;
	}


	//	@NonNull
	//	public <T> UnidirectionalBinding from(@NonNull IObservableValue< ? > sourceo) {
	//		return new UnidirectionalBinding(this, sourceo);
	//	}

	/**
	 * Start for a bidirectional binding: if either side changes it updates the other. The
	 * other side is defined with {@link #to(Object, String)}.
	 * @param source
	 * @param property
	 * @return
	 */
	@NonNull
	public <S, T, V> JoinBinding joinbinding(@NonNull S source, @NonNull String sproperty, @NonNull final T target, @NonNull final String tproperty) throws Exception {
		if(null == source || null == sproperty)
			throw new IllegalArgumentException("source/property cannot be null");
		if(null == target || null == tproperty)
			throw new IllegalArgumentException("target/property cannot be null");

		IObservableValue< ? > sourceo = createObservable(source, sproperty);
		IObservableValue<V> targeto = (IObservableValue<V>) BindingContext.createObservable(target, tproperty);

		//-- If target is a NodeBase then bind an error listener too
		if(target instanceof NodeBase) {
			NodeBase nb = (NodeBase) target;
			nb.setMessageBroadcastEnabled(false);
			bindMessage(source, sproperty, nb);
		}

		return new JoinBinding(this, sourceo, targeto);
	}

	@NonNull
	public <S, T, V> JoinBinding joinbinding(@NonNull IObservableValue<S> sourceo, @NonNull final T target, @NonNull final String tproperty) throws Exception {
		if(null == target || null == tproperty)
			throw new IllegalArgumentException("target/property cannot be null");

		IObservableValue<V> targeto = (IObservableValue<V>) BindingContext.createObservable(target, tproperty);

		//-- If target is a NodeBase then bind an error listener too
		if(target instanceof NodeBase) {
			NodeBase nb = (NodeBase) target;
			nb.setMessageBroadcastEnabled(false);
			//			bindMessage(source, sproperty, nb);
		}

		return new JoinBinding(this, sourceo, targeto);
	}


	//	/**
	//	 * Bidirectional binding of a source Observable to some target.
	//	 */
	//	public <T> JoinBinding join(@NonNull IObservableValue< ? > sourceo) {
	//		return new JoinBinding(this, sourceo);
	//	}

	/**
	 * Start a listening option.
	 * @param source
	 * @param property
	 * @return
	 */
	@NonNull
	public <T> Listener onchange(@NonNull T source, @NonNull String... properties) {
		IObservableValue< ? >[] obsar = new IObservableValue< ? >[properties.length];
		for(int i = properties.length; --i >= 0;) {
			obsar[i] = createObservable(source, properties[i]);
		}
		return new Listener(obsar);
	}

	@NonNull
	static <T> IObservableValue< ? > createObservable(@NonNull T source, @NonNull String property) {
		if(source instanceof IObservableEntity) {
			IObservableEntity oe = (IObservableEntity) source;
			IObservableValue< ? > op = oe.observableValue(property);
			return op;
		}

		throw new IllegalArgumentException("The class  " + source.getClass() + " is not Observable.");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Errors.												*/
	/*--------------------------------------------------------------*/

	/**
	 * Bind the "message" property of a control to this context, so that problems in the control are propagated
	 * to this binding context, and
	 * @param instance
	 * @param property
	 * @param control
	 */
	public <T> void bindMessage(@NonNull T instance, @NonNull String property, @NonNull NodeBase control) {
//		Map<String, ErrorBinding> map = getInstanceErrorMap(instance);
//		ErrorBinding eb = map.remove(property);
//		if(null != eb) {
//			eb.dispose();
//		}
//
//		IObservableValue<UIMessage> uimo = (IObservableValue<UIMessage>) control.observableValue("message");
//		eb = new ErrorBinding(this, uimo);
//		map.put(property, eb);
	}

	@NonNull
	private <T> Map<String, ErrorBinding> getInstanceErrorMap(@NonNull T instance) {
		Map<String, ErrorBinding> map = m_errorBindingMap.get(instance);
		if(null == map) {
			map = new HashMap<String, ErrorBinding>();
			m_errorBindingMap.put(instance, map);
		}
		return map;
	}

	/**
	 * Callable by business logic, this notifies that an error has occurred or was cleared on some object.
	 * @param instance
	 * @param property
	 * @param error
	 */
	public <T> void setProperyError(@NonNull T instance, @NonNull String property, @Nullable UIMessage error) throws Exception {
		Map<String, ErrorBinding> map = getInstanceErrorMap(instance);
		ErrorBinding eb = map.get(property);
		if(null != eb) {
			//-- report the error on the control.
			eb.setMessage(error);
		} else if(error != null) {
			eb = new ErrorBinding(this, error);
			map.put(property, eb);
		}
	}

	/**
	 * If a binding's getValue() or setValue() call fails with a validation exception this method gets called to
	 * change the error state of the property, if possible.
	 * @param binding
	 * @param uiMessage
	 */
	void bindingError(@NonNull Binding binding, @Nullable UIMessage uiMessage) {

	}

	/**
	 * When a component's error state changes this gets called by the {@link ErrorBinding} associated
	 * with the control. It registers or clears the error state of the associated data item.
	 *
	 * @param errorBinding
	 * @param old
	 * @param new1
	 */
	public void errorBindingChanged(@NonNull ErrorBinding errorBinding, @Nullable UIMessage old, @Nullable UIMessage new1) {
		// TODO Auto-generated method stub

	}
}
