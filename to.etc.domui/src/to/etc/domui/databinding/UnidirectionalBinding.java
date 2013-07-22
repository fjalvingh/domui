package to.etc.domui.databinding;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

/**
 * Unidirectional bind from a -&gt; b.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 30, 2013
 */
public class UnidirectionalBinding extends Binding {
	private IValueChangeListener< ? > m_fromListener;

	UnidirectionalBinding(@Nonnull BindingContext context, @Nonnull IObservableValue< ? > sourceo) {
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
	public <T, V> UnidirectionalBinding to(@Nonnull final T target, @Nonnull String property) throws Exception {
		if(null == target || null == property)
			throw new IllegalArgumentException("target/property cannot be null");

		//-- Unidirectional binds only need to have access to a setter, we do not need an Observable.
		final PropertyMetaModel<V> pmm = (PropertyMetaModel<V>) MetaManager.getPropertyMeta(target.getClass(), property);
		m_tomodel = new IReadWriteModel<V>() {
			@Override
			public V getValue() throws Exception {
				throw new IllegalStateException("Unexpected 'get' in unidirectional binding");
			}

			@Override
			public void setValue(V value) throws Exception {
				pmm.setValue(target, value);
			}
		};
		//
		//			IObservableValue<V> targeto = (IObservableValue<V>) createObservable(target, property);
		//			m_to = targeto;
		addSourceListener();

		//-- Immediately move the value of source to target too 2
		moveSourceToTarget();
		return this;
	}

	public <F, T> UnidirectionalBinding convert(@Nonnull IUniConverter<F, T> converter) {
		m_converter = converter;
		return this;
	}
}