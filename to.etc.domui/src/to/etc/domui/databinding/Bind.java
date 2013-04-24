package to.etc.domui.databinding;

import javax.annotation.*;

/**
 * Static helper to create bindings using the IObservable model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 24, 2013
 */
public class Bind {
	@Nonnull
	final private IObservableValue< ? > m_from;

	final boolean m_bidirectional;

	private IObservableValue< ? > m_to;

	private IValueChangeListener< ? > m_fromListener;

	private IValueChangeListener< ? > m_toListener;

	private Bind(@Nonnull IObservableValue< ? > sourceo) {
		this(sourceo, false);
	}

	private Bind(@Nonnull IObservableValue< ? > sourceo, boolean bidirectional) {
		m_from = sourceo;
		m_bidirectional = bidirectional;
	}

	/**
	 * Start for an "unidirectional" binding: Bind.from().to();
	 * @param source
	 * @param property
	 * @return
	 */
	static public <T> Bind from(@Nonnull T source, @Nonnull String property) {
		if(null == source || null == property)
			throw new IllegalArgumentException("source/property cannot be null");
		IObservableValue< ? > sourceo = createObservable(source, property);
		return new Bind(sourceo);
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
	public <T, V> Bind to(@Nonnull T target, @Nonnull String property) throws Exception {
		if(null == target || null == property)
			throw new IllegalArgumentException("target/property cannot be null");
		IObservableValue<V> targeto = (IObservableValue<V>) createObservable(target, property);
		m_to = targeto;
		addSourceListener(targeto);
		if(m_bidirectional)
			addTargetListener(m_from);

		//-- Immediately move the value of source to target too
		V val = ((IObservableValue<V>) m_from).getValue();
		((IObservableValue<V>) m_to).setValue(val);
		return this;
	}

	/**
	 * Start for a bidirectional binding: if either side changes it updates the other. The
	 * other side is defined with {@link #to(Object, String)}.
	 * @param source
	 * @param property
	 * @return
	 */
	@Nonnull
	static public <T> Bind join(@Nonnull T source, @Nonnull String property) {
		if(null == source || null == property)
			throw new IllegalArgumentException("source/property cannot be null");
		IObservableValue< ? > sourceo = createObservable(source, property);
		return new Bind(sourceo);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Event listener.										*/
	/*--------------------------------------------------------------*/

	static public final class Listener {
		@Nonnull
		final private IBindingListener m_listener;

		private int m_recurse;

		public Listener(IBindingListener listener) {
			m_listener = listener;
		}

		@Nonnull
		public <T, V> Listener on(@Nonnull T instance, @Nonnull String... properties) {
			for(String s : properties) {
				IObservableValue<V> o = (IObservableValue<V>) createObservable(instance, s);
				o.addChangeListener(new IValueChangeListener<V>() {
					@Override
					public void handleChange(ValueChangeEvent<V> event) throws Exception {
						if(m_recurse > 0)
							return;
						try {
							m_recurse++;
							m_listener.valuesChanged();
						} finally {
							m_recurse--;
						}
					}
				});
			}

			return this;
		}
	}

	@Nonnull
	static public Listener listener(@Nonnull IBindingListener lsnr) {
		return new Listener(lsnr);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Helper code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a listener on FROM, so that changes there propagate to TO.
	 * @param value
	 */
	private <V> void addSourceListener(@Nonnull IObservableValue<V> value) {
		IValueChangeListener<V> ml = new IValueChangeListener<V>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<V> event) throws Exception {
				V nwval = event.getDiff().getNew();
				((IObservableValue<V>) m_to).setValue(nwval);
			}
		};
		m_fromListener = ml;
		((IObservableValue<V>) m_from).addChangeListener(ml);
	}

	/**
	 * Add a listener on FROM, so that changes there propagate to TO.
	 * @param value
	 */
	private <V> void addTargetListener(@Nonnull IObservableValue<V> value) {
		IValueChangeListener<V> ml = new IValueChangeListener<V>() {
			@Override
			public void handleChange(@Nonnull ValueChangeEvent<V> event) throws Exception {
				V nwval = event.getDiff().getNew();
				((IObservableValue<V>) m_from).setValue(nwval);
			}
		};
		m_toListener = ml;
		((IObservableValue<V>) m_to).addChangeListener(ml);
	}

	@Nonnull
	static private <T> IObservableValue< ? > createObservable(@Nonnull T source, @Nonnull String property) {
		if(source instanceof IObservableEntity) {
			IObservableEntity oe = (IObservableEntity) source;
			IObservableValue< ? > op = oe.observableProperty(property);
			return op;
		}

		throw new IllegalArgumentException("The class " + source.getClass() + " is not Observable.");
	}

}
