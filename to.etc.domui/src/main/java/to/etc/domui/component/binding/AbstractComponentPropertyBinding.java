package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IReadOnlyModel;
import to.etc.domui.util.IValueAccessor;
import to.etc.domui.util.IWriteOnlyModel;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1-4-18.
 */
@NonNullByDefault
abstract class AbstractComponentPropertyBinding<C extends NodeBase, CV, M, MV> implements IBinding {
	@NonNull
	final protected C m_control;

	@NonNull
	final protected PropertyMetaModel<CV> m_controlProperty;

	/** The instance bound to */
	//@Nullable
	final private M m_instance;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	//@Nullable
	final private IValueAccessor<MV> m_instanceProperty;

	/**
	 * The last value that was moved to the control. See the comment in moveModelToControl below.
	 */
	@Nullable
	protected MV m_lastValueFromControlAsModelValue;

	/** If this binding is in error this contains the error. */
	@Nullable
	protected UIMessage m_bindError;

	@Nullable
	private IWriteOnlyModel<MV> m_setter;

	@Nullable
	private IReadOnlyModel<MV> m_getter;

	protected AbstractComponentPropertyBinding(C control, PropertyMetaModel<CV> controlProperty, M modelInstance, IValueAccessor<MV> accessor) {
		m_control = control;
		m_controlProperty = controlProperty;
		m_instance = modelInstance;
		m_instanceProperty = accessor;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Accessing the binding's data.								*/
	/*----------------------------------------------------------------------*/
	/**
	 * If this binding is in error: return the message describing that error.
	 * @return
	 */
	@Override
	@Nullable
	public UIMessage getBindError() {
		return m_bindError;
	}

	@NonNull
	public IValueAccessor< ? > getControlProperty() {
		return m_controlProperty;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("binding[");
		if(m_instance != null) {
			sb.append(m_instance);
		} else if(m_setter != null || m_getter != null) {
			sb.append("get/set lambda");
		} else {
			sb.append("?");
		}
		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(instanceProperty != null) {
			sb.append(".");
			if(instanceProperty instanceof PropertyMetaModel) {
				sb.append(((PropertyMetaModel< ? >) instanceProperty).getName());
			} else {
				sb.append(instanceProperty.toString());
			}
		}
		NodeBase control = m_control;
		if(null != control) {
			sb.append(" to ");
			sb.append(control.getClass().getSimpleName());
			IValueAccessor<?> controlProperty = m_controlProperty;
			if(null != controlProperty) {
				if(controlProperty instanceof PropertyMetaModel<?>) {
					sb.append(".").append(((PropertyMetaModel<?>) controlProperty).getName());
				} else {
					sb.append(controlProperty.toString());
				}
			}
		}
		sb.append("]");
		return sb.toString();
	}

	//@Nullable
	public M getInstance() {
		return m_instance;
	}

	//@Nullable
	public IValueAccessor<MV> getInstanceProperty() {
		return m_instanceProperty;
	}


	@Nullable
	protected MV getValueFromModel() throws Exception {
		MV modelValue;
		IReadOnlyModel<MV> getter = m_getter;
		if(null != getter) {
			modelValue = getter.getValue();
		} else {
			IValueAccessor<MV> instanceProperty = m_instanceProperty;
			if(null == instanceProperty)
				throw new IllegalStateException("instance property cannot be null");
			M instance = m_instance;
			if(null == instance)
				throw new IllegalStateException("instance cannot be null");
			modelValue = instanceProperty.getValue(instance);
		}
		return modelValue;
	}

	@Override public <T> void setModelValue(@Nullable T value) {
		IWriteOnlyModel<T> setter = (IWriteOnlyModel<T>) m_setter;
		if(m_getter != null || setter != null) {
			if(setter != null) {
				try {
					setter.getValue(value);
				} catch(Exception x) {
					if(value == null)
						throw new BindingFailureException(x, "->model", this + ": Binding error moving null to the binding's 'set' lambda");
					throw new BindingFailureException(x, "->model", this + ": Binding error moving " + value + " (a " + value.getClass().getName() + ") to the binding's 'set' lambda");
				}
			}
			return;
		}

		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
		if(instanceProperty.isReadOnly())
			throw new IllegalStateException(instanceProperty + ": You cannot set this read-only property");
		Object instance = m_instance;
		if(null == instance)
			throw new IllegalStateException("instance cannot be null");

		try {
			((IValueAccessor<T>) instanceProperty).setValue(instance, value);
		} catch(Exception x) {
			if(value == null)
				throw new BindingFailureException(x, "->model", this + ": Binding error moving null to " + m_instanceProperty);
			throw new BindingFailureException(x, "->model", this + ": Binding error moving " + value + " (a " + value.getClass().getName() + ") to " + m_instanceProperty);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IModelBinding interface implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Move the data to the control.
	 */
	@Override
	public void moveModelToControl() throws Exception {
		try {
			MV modelValue = getValueFromModel();

			// FIXME We should think about exception handling here
			//System.out.println("binder: set "+control.getComponentInfo()+" value="+modelValue);
			/*
			 * We only update the control if the model value /changed/ since the last time
			 * the value was moved to the control. If the value in the model did not change
			 * then leave the control as-is. This is needed for properly reporting binding
			 * errors: if the value in a control is changed so that it causes a binding
			 * error then the return value for the control is null. If we would then set the
			 * model value /back/ in the control then the binding error would be overwritten.
			 */
			if(!MetaManager.areObjectsEqual(modelValue, m_lastValueFromControlAsModelValue)) {
				//-- Value in instance differs from control's
				m_lastValueFromControlAsModelValue = modelValue;
				CV controlValue = convertModelToControl(modelValue);
				if(m_controlProperty.getReadOnly() != YesNoType.YES) {
					m_controlProperty.setValue(m_control, controlValue);
//					System.out.println(this + ": m2c " + controlValue);
				}
				m_bindError = null;                                    // Let's assume binding has no trouble.
			}
		} catch(Exception x) {
			throw new BindingFailureException(x, "Model->Control", this.toString());
		}
	}

	@Nullable
	abstract protected CV convertModelToControl(@Nullable MV modelValue) throws Exception;
}
