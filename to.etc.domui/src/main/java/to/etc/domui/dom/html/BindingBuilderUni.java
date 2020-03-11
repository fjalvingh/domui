package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.BindReference;
import to.etc.domui.component.binding.BindingDefinitionException;
import to.etc.domui.component.binding.CalculatedBinding;
import to.etc.domui.component.binding.ComponentPropertyBindingUni;
import to.etc.domui.component.input.ITypedControl;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.util.Documentation;
import to.etc.domui.util.DomUtil;
import to.etc.function.FunctionEx;
import to.etc.function.SupplierEx;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.query.QField;

/**
 *
 *
 * @param <CV> 	The type of the value of the control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-3-18.
 */
final public class BindingBuilderUni<CV> {
	@NonNull
	final private NodeBase m_control;

	@NonNull
	final private PropertyMetaModel<CV> m_controlProperty;

	BindingBuilderUni(@NonNull NodeBase control, @NonNull String controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = (PropertyMetaModel<CV>) MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	BindingBuilderUni(@NonNull NodeBase control, @NonNull QField<?, CV> controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.getName().contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	BindingBuilderUni(@NonNull NodeBase control, @NonNull PropertyMetaModel<CV> controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.getName().contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see " + Documentation.BINDING_NO_DOTTED_PATH);
		m_control = control;
		m_controlProperty = controlProperty;
	}

	public <M, MV> ComponentPropertyBindingUni<?, CV, M, MV> to(@NonNull BindReference<M, MV> ref) throws Exception {
		return to(ref.getInstance(), ref.getProperty());
	}

	public <M, MV> ComponentPropertyBindingUni<?, CV, M, MV> to(@NonNull BindReference<M, MV> ref, @Nullable FunctionEx<MV, CV> converter) throws Exception {
		return to(ref.getInstance(), ref.getProperty(), converter);
	}

	public <M, MV> ComponentPropertyBindingUni<?, CV, M, MV> to(@NonNull M instance, @NonNull String property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, (PropertyMetaModel<MV>) MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <M, MV> ComponentPropertyBindingUni<?, CV, M, MV> to(@NonNull M instance, @NonNull String property, @Nullable FunctionEx<MV, CV> converter) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, (PropertyMetaModel<MV>) MetaManager.getPropertyMeta(instance.getClass(), property), converter);
	}

	public <T, MV> ComponentPropertyBindingUni<?, CV, T, MV> to(@NonNull T instance, @NonNull QField<?, MV> property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	public <T, MV> ComponentPropertyBindingUni<?, CV, T, MV> to(@NonNull T instance, @NonNull QField<?, MV> property, @Nullable FunctionEx<MV, CV> converter) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		return to(instance, MetaManager.getPropertyMeta(instance.getClass(), property), converter);
	}

	public <T, MV> ComponentPropertyBindingUni<?, CV, T, MV> to(@NonNull T instance, @NonNull PropertyMetaModel<MV> pmm) throws Exception {
		return to(instance, pmm, null);
	}

	/**
	 * Bind to a IValueAccessor and the given instance.
	 */
	public <T, MV> ComponentPropertyBindingUni<?, CV, T, MV> to(@NonNull T instance, @NonNull PropertyMetaModel<MV> pmm, @Nullable FunctionEx<MV, CV> converter) throws Exception {
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");

		//-- Check: are the types of the binding ok?
		if(pmm instanceof PropertyMetaModel<?> && converter == null) {
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
		ComponentPropertyBindingUni<?, CV, T, MV> binding = new ComponentPropertyBindingUni<>(m_control, m_controlProperty, instance, pmm, converter);
		binding.moveModelToControl();
		m_control.finishBinding(binding);
		return binding;
	}

	public CalculatedBinding<CV> to(SupplierEx<Object> acceptor) {
		CalculatedBinding<CV> calculatedBinding = new CalculatedBinding<>(m_control, m_controlProperty, acceptor);
		m_control.finishBinding(calculatedBinding);
		return calculatedBinding;
	}

	@Override public String toString() {
		return "binding from " + m_control + "." + m_controlProperty.getName();
	}
}
