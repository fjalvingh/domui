package to.etc.domui.dom.html;

import to.etc.domui.component.binding.BindReference;
import to.etc.domui.component.binding.ComponentPropertyBinding;
import to.etc.domui.component.binding.IBidiBindingConverter;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.IValueAccessor;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-3-18.
 */
final public class ConvertingBindingBuilder<C, M> {
	private final NodeBase m_control;

	private final PropertyMetaModel<C> m_controlProperty;

	private final IBidiBindingConverter<C, M> m_converter;

	public ConvertingBindingBuilder(NodeBase control, PropertyMetaModel<C> controlProperty, IBidiBindingConverter<C, M> converter) {
		m_control = control;
		m_controlProperty = controlProperty;
		m_converter = converter;
	}

	public <M> ComponentPropertyBinding to(@Nonnull BindReference<?, M> ref) throws Exception {
		return to(ref.getInstance(), ref.getProperty());
	}

	public <T, M> ComponentPropertyBinding to(@Nonnull T instance, @Nonnull String property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Bind to a IValueAccessor and the given instance.
	 */
	public <T, M> ComponentPropertyBinding to(@Nonnull T instance, @Nonnull IValueAccessor<M> pmm) throws Exception {
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");

		//-- For a converting binding we cannot check anything because of type erasure's stupidity.

		//-- Move the data now!
		ComponentPropertyBinding binding = new ComponentPropertyBinding(m_control, m_controlProperty, instance, pmm);
		binding.moveModelToControl();
		m_control.finishBinding(binding);
		return binding;
	}
}
