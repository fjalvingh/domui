package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.meta.PropertyMetaModel;

/**
 * A reference to an instance and property that is the source or the target of some binding.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-17.
 */
@NonNullByDefault
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
