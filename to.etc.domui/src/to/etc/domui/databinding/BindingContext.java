package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;

/**
 * Maintains all bindings, and their validation/error status.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 21, 2013
 */
public class BindingContext {

	/**
	 * Create a binding between unnamed entities.
	 * @param source
	 * @param to
	 * @return
	 */
	public <T> Binding join(@Nonnull IObservableValue<T> source, @Nonnull IObservableValue<T> to) {

		throw new IllegalStateException();
	}

	void registerBinding(@Nonnull Binding binding) {

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

		public Listener(@Nonnull IObservableValue< ? >[] obsar) {
			m_observables = obsar;
		}

		@Nonnull
		public Listener call(@Nonnull final IBindingListener lsnr) {
			IValueChangeListener<Object> ovs = new IValueChangeListener<Object>() {
				@Override
				public void handleChange(@Nonnull ValueChangeEvent<Object> event) throws Exception {
					if(m_recurse > 0)
						return;
					try {
						m_recurse++;
						lsnr.valuesChanged();
					} finally {
						m_recurse--;
					}
				}

				@Override
				public void setError(UIMessage error) {
					throw new IllegalStateException("Implement me, please");
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
	public <T> UnidirectionalBinding from(@Nonnull T source, @Nonnull String property) {
		if(null == source || null == property)
			throw new IllegalArgumentException("source/property cannot be null");
		IObservableValue< ? > sourceo = createObservable(source, property);
		return new UnidirectionalBinding(this, sourceo);
	}

	public <T> UnidirectionalBinding from(@Nonnull IObservableValue< ? > sourceo) {
		return new UnidirectionalBinding(this, sourceo);
	}

	/**
	 * Start for a bidirectional binding: if either side changes it updates the other. The
	 * other side is defined with {@link #to(Object, String)}.
	 * @param source
	 * @param property
	 * @return
	 */
	@Nonnull
	public <T> JoinBinding join(@Nonnull T source, @Nonnull String property) {
		if(null == source || null == property)
			throw new IllegalArgumentException("source/property cannot be null");
		IObservableValue< ? > sourceo = createObservable(source, property);
		return new JoinBinding(this, sourceo);
	}

	/**
	 * Bidirectional binding of a source Observable to some target.
	 */
	public <T> JoinBinding join(@Nonnull IObservableValue< ? > sourceo) {
		return new JoinBinding(this, sourceo);
	}

	/**
	 * Start a listening option.
	 * @param source
	 * @param property
	 * @return
	 */
	@Nonnull
	public <T> Listener onchange(@Nonnull T source, @Nonnull String... properties) {
		IObservableValue< ? >[] obsar = new IObservableValue< ? >[properties.length];
		for(int i = properties.length; --i >= 0;) {
			obsar[i] = createObservable(source, properties[i]);
		}
		return new Listener(obsar);
	}

	@Nonnull
	static <T> IObservableValue< ? > createObservable(@Nonnull T source, @Nonnull String property) {
		if(source instanceof IObservableEntity) {
			IObservableEntity oe = (IObservableEntity) source;
			IObservableValue< ? > op = oe.observableProperty(property);
			return op;
		}

		throw new IllegalArgumentException("The class  " + source.getClass() + " is not Observable.");
	}

	/**
	 *
	 * @param binding
	 * @param old
	 * @param nw
	 */
	public void bindingErrorChanged(@Nonnull Binding binding, @Nullable UIMessage old, @Nullable UIMessage nw) {
		System.out.println("binding error change: " + binding + " from " + old + " to " + nw);

	}


}
