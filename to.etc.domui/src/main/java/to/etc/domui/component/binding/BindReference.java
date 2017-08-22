package to.etc.domui.component.binding;

import to.etc.domui.component.meta.PropertyMetaModel;

import javax.annotation.DefaultNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
@DefaultNonNull
final public class BindReference<I, V> {
	private final I m_instance;

	private final PropertyMetaModel<V> m_property;

	public BindReference(I instance, PropertyMetaModel<V> property) {
		m_instance = instance;
		m_property = property;
	}

	public I getInstance() {
		return m_instance;
	}

	public PropertyMetaModel<V> getProperty() {
		return m_property;
	}
}
