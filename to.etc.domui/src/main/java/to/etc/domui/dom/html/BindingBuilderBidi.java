package to.etc.domui.dom.html;

import to.etc.domui.component.binding.BindReference;
import to.etc.domui.component.binding.BindingDefinitionException;
import to.etc.domui.component.binding.ComponentPropertyBinding;
import to.etc.domui.component.binding.IBidiBindingConverter;
import to.etc.domui.component.input.ITypedControl;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.Documentation;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IValueAccessor;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 *
 * @param <CV> 	The type of the value of the control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-3-18.
 */
final public class BindingBuilderBidi<CV> {
	@Nonnull
	final private NodeBase m_control;

	@Nonnull
	final private PropertyMetaModel<CV> m_controlProperty;

	BindingBuilderBidi(@Nonnull NodeBase control, @Nonnull String controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = (PropertyMetaModel<CV>) MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	BindingBuilderBidi(@Nonnull NodeBase control, @Nonnull QField<?, CV> controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.getPath().contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	BindingBuilderBidi(@Nonnull NodeBase control, @Nonnull PropertyMetaModel<CV> controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.getName().contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = controlProperty;
	}

	public <M, MV> ComponentPropertyBinding<?, CV, M, MV> to(@Nonnull BindReference<M, MV> ref) throws Exception {
		return to(ref.getInstance(), ref.getProperty());
	}

	public <M, MV> ComponentPropertyBinding<?, CV, M, MV> to(@Nonnull BindReference<M, MV> ref, @Nullable IBidiBindingConverter<CV, MV> converter) throws Exception {
		return to(ref.getInstance(), ref.getProperty(), converter);
	}

	public <M, MV> ComponentPropertyBinding<?, CV, M, MV> to(@Nonnull M instance, @Nonnull String property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, (PropertyMetaModel<MV>) MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <M, MV> ComponentPropertyBinding<?, CV, M, MV> to(@Nonnull M instance, @Nonnull String property, @Nullable IBidiBindingConverter<CV, MV> converter) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, (PropertyMetaModel<MV>) MetaManager.getPropertyMeta(instance.getClass(), property), converter);
	}

	public <T, MV> ComponentPropertyBinding<?, CV, T, MV> to(@Nonnull T instance, @Nonnull QField<?, MV> property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <T, MV> ComponentPropertyBinding<?, CV, T, MV> to(@Nonnull T instance, @Nonnull QField<?, MV> property, @Nullable IBidiBindingConverter<CV, MV> converter) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property), converter);
	}

	public <T, MV> ComponentPropertyBinding<?, CV, T, MV> to(@Nonnull T instance, @Nonnull IValueAccessor<MV> pmm) throws Exception {
		return to(instance, pmm, null);
	}

	/**
	 * Bind to a IValueAccessor and the given instance.
	 */
	public <T, MV> ComponentPropertyBinding<?, CV, T, MV> to(@Nonnull T instance, @Nonnull IValueAccessor<MV> pmm, @Nullable IBidiBindingConverter<CV, MV> converter) throws Exception {
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");

		//-- Check: are the types of the binding ok?
		if(pmm instanceof PropertyMetaModel<?>) {
			PropertyMetaModel<?> p = (PropertyMetaModel<?>) pmm;
			Class<?> actualType = DomUtil.getBoxedForPrimitive(p.getActualType());
			Class<?> controlType = DomUtil.getBoxedForPrimitive(m_controlProperty.getActualType());

			if(controlType == Object.class) {
				//-- Type erasure, deep, deep sigh. Can the control tell us the actual type contained?
				if(m_control instanceof ITypedControl) {
					ITypedControl<?> typedControl = (ITypedControl<?>) m_control;
					controlType = DomUtil.getBoxedForPrimitive(typedControl.getActualType());
				}
			}

			/*
			 * For properties that have a generic type the Java "architects" do type erasure, so we cannot check anything. Type safe my ...
			 */
			if(actualType != Object.class && controlType != Object.class) {
				if(!actualType.isAssignableFrom(controlType))
					throw new BindingDefinitionException(toString(), actualType.getName(), controlType.getName());

				if(!controlType.isAssignableFrom(actualType))
					throw new BindingDefinitionException(toString(), actualType.getName(), controlType.getName());
			}
		}

		//-- Move the data now!
		ComponentPropertyBinding<?, CV, T, MV> binding = new ComponentPropertyBinding<>(m_control, m_controlProperty, instance, pmm, converter);
		binding.moveModelToControl();
		m_control.finishBinding(binding);
		return binding;
	}
}
