/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.binding;

import to.etc.domui.component.input.ITypedControl;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IReadOnlyModel;
import to.etc.domui.util.IValueAccessor;
import to.etc.domui.util.IWriteOnlyModel;
import to.etc.domui.util.Msgs;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.nls.CodeException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This binds a control property to some model property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
@DefaultNonNull
final public class ComponentPropertyBinding implements IBinding {
	@Nonnull
	final private NodeBase m_control;

	@Nonnull
	final private PropertyMetaModel< ? > m_controlProperty;

	/** The instance bound to */
	@Nullable
	private Object m_instance;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	@Nullable
	private IValueAccessor< ? > m_instanceProperty;

	/** If this thing is bound to some event listener... */
	@Nullable
	private IBindingListener< ? > m_listener;

	@Nullable
	private IBindingConverter<?, ?> m_converter;

	/**
	 * The last value read from the control. If a converter is present, this value is converted to a MODEL value.
	 */
	@Nullable
	private Object m_lastValueFromControlAsModelValue;

	/** If this binding is in error this contains the error. */
	@Nullable
	private UIMessage m_bindError;

	@Nullable
	private IWriteOnlyModel<?> m_setter;

	@Nullable
	private IReadOnlyModel<?> m_getter;

	public ComponentPropertyBinding(@Nonnull NodeBase control, @Nonnull String controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		if(controlProperty.contains("."))
			throw new ProgrammerErrorException("You cannot bind a Control property dotted path, see "+ DomUtil.DOCROOT+"x/GYA-/");
		m_control = control;
		m_controlProperty = MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	private void checkAssigned() {
		if(m_listener != null || m_instance != null || m_setter != null || m_getter != null)
			throw new ProgrammerErrorException("This binding is already fully defined. Create a new one.");
	}
	private void checkNonSetterAssigned() {
		if(m_listener != null || m_instance != null)
			throw new ProgrammerErrorException("This binding is already fully defined. Create a new one.");
	}

	@Nonnull
	public ComponentPropertyBinding convert(@Nullable IBindingConverter<?, ?> converter) {
		checkAssigned();
		if(m_converter != null) {
			throw new ProgrammerErrorException("This binding already has a converter specified");
		}
		m_converter = converter;
		return this;
	}

	public void to(@Nonnull IBindingListener< ? > listener) {
		checkAssigned();
		if(listener == null)
			throw new IllegalArgumentException("Argument cannot be null");
		m_listener = listener;
	}

	public void to(@Nonnull BindReference<?, ?> ref) throws Exception {
		checkAssigned();
		to(ref.getInstance(), ref.getProperty());
	}

	public <T> void to(@Nonnull T instance, @Nonnull String property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Define a setter method for this binding, which gets called when the component value changes.
	 *
	 * @param writer
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> ComponentPropertyBinding set(IWriteOnlyModel<T> writer) {
		checkNonSetterAssigned();
		if(m_setter != null)
			throw new ProgrammerErrorException("This binding already as a setter defined through a 'set' call");
		m_setter = writer;
		return this;
	}

	/**
	 * Define a getter method for this binding, which get called to get the current value from the
	 * model object when it's time to move the model back to the control.
	 *
	 * @param reader
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> ComponentPropertyBinding get(IReadOnlyModel<T> reader) throws Exception {
		checkNonSetterAssigned();
		if(m_getter != null)
			throw new ProgrammerErrorException("This binding already as a getter defined through a 'get' call");
		m_getter = reader;
		moveModelToControl();							// Immediately initialize the control's value
		return this;
	}


	/**
	 * Bind to a IValueAccessor and the given instance.
	 * @param instance
	 * @param pmm
	 * @throws Exception
	 */
	public <T, V> void to(@Nonnull T instance, @Nonnull IValueAccessor<V> pmm) throws Exception {
		checkAssigned();
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");
		m_instanceProperty = pmm;
		m_instance = instance;

		//-- Check: are the types of the binding ok?
		if(pmm instanceof PropertyMetaModel<?> && m_converter == null) {
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
		moveModelToControl();
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

	@Nonnull
	public IValueAccessor< ? > getControlProperty() {
		return m_controlProperty;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("binding[");
		if(m_instance != null) {
			sb.append(m_instance);
		} else if(m_listener != null) {
			sb.append("listener ").append(m_listener);
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

	@Nullable
	public Object getInstance() {
		return m_instance;
	}

	@Nullable
	public IValueAccessor< ? > getInstanceProperty() {
		return m_instanceProperty;
	}


	@Nullable
	private Object getValueFromModel() throws Exception {
		Object modelValue;
		IReadOnlyModel<?> getter = m_getter;
		if(null != getter) {
			modelValue = getter.getValue();
		} else {
			IValueAccessor<?> instanceProperty = m_instanceProperty;
			if(null == instanceProperty)
				throw new IllegalStateException("instance property cannot be null");
			Object instance = m_instance;
			if(null == instance)
				throw new IllegalStateException("instance cannot be null");
			modelValue = instanceProperty.getValue(instance);
		}
		return modelValue;
	}

	/**
	 *
	 * @param value
	 * @param <T>
	 */
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
	 * Calculate the list of changes made to controls, as part one of the controlToModel
	 * process. Each control whose value changed will be registered in a list of
	 * {@link BindingValuePair} instances which will also contain any error message.
	 *
	 * This is the *hard* part of binding: it needs to handle control errors caused by bindValue() throwing
	 * an exception.
	 */
	@Override
	@Nullable
	public BindingValuePair<?, ?> getBindingDifference() throws Exception {
		NodeBase control = m_control;
		if(control instanceof IDisplayControl)
			return null;

		/*
		 * jal 20150414 Readonly (display) and disabled controls should not bind their value
		 * back to the model. This solves the following problem (at least for these kind of
		 * fields): take a model that has two Text<Integer> controls: one editable bound to
		 * property a, and one readonly bound to b.
		 * The setter for 'a' calculates a new value for b somehow (like b = a + 12).
		 *
		 * When the screen renders a will be 0 and b will be 12, so the Text controls represent
		 * that. Now when Text for a changes to 10 the following happens:
		 * - 10 gets moved to setA(), and this calls setB(22). So property b is now 22.
		 * - 12 gets moved to setB() from the _unchanged_ Text<> from b, so overwriting the new value.
		 * This cause of events is clearly wrong for readonly/disabled fields, so we disable
		 * them from updating the model.
		 *
		 * The general case, where both controls are editable, amounts to whom should
		 * win in an update: if Text<b> changed due to a user and Text<A> also changed
		 * and caused an update to b - which update is "the most important"? This is
		 * not yet solved (but might be by letting either model or UI win in case of a
		 * conflicting model update).
		 */
		if(control instanceof IControl) {
			IControl<?> ict = (IControl<?>) control;
			if(ict.isDisabled() || ict.isReadOnly()) {
				return null;
			}
		}

		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveControlToModel(control);
			return null;
		}

		/*
		 * Get the control's value. If the control is in error (validation/conversion) then
		 * add the problem inside the Error collector, signaling a problem to any logic
		 * that would run after.
		 */
		Object controlValue = null;
		UIMessage newError = null;
		try {
			controlValue = m_controlProperty.getValue(m_control);
			IBindingConverter<?, ?> converter = m_converter;
			if(converter != null) {
				controlValue = ((IBindingConverter<Object, Object>)converter).controlToModel(controlValue);
			}
			m_lastValueFromControlAsModelValue = controlValue;
			m_bindError = null;
		} catch(CodeException cx) {
			newError = UIMessage.error(cx);
			newError.setErrorNode(control);
			newError.setErrorLocation(control.getErrorLocation());
			if(!newError.equals(control.getMessage())) {
				m_bindError = newError;
			}
			//System.out.println("~~ " + control + " to " + instanceProperty + ": " + cx);
		}

		//-- When in error we cannot set anything anyway, so exit.
		if(null != newError && !newError.getCode().equals(Msgs.MANDATORY)) {
			/*
			 * jal 20171018 When a mandatory LookupInput gets cleared its value becomes null, and this
			 * value should be propagated to the model. It seems likely that in ALL cases of error
			 * we need to move a null there!
			 */

			return null;
		}

		Object propertyValue = getValueFromModel();
		if(MetaManager.areObjectsEqual(propertyValue, controlValue))
			return null;

		return new BindingValuePair<>(this, controlValue);
	}

	/**
	 *
	 * @throws Exception
	 */
	@Override
	public void moveModelToControl() throws Exception {
		try {
			IBindingListener<?> listener = m_listener;
			if(listener != null) {
				((IBindingListener<NodeBase>) listener).moveModelToControl(m_control);
				return;
			}
			Object modelValue = getValueFromModel();

			// FIXME We should think about exception handling here
			//System.out.println("binder: set "+control.getComponentInfo()+" value="+modelValue);
			if(!MetaManager.areObjectsEqual(modelValue, m_lastValueFromControlAsModelValue)) {
				//-- Value in instance differs from control's
				m_lastValueFromControlAsModelValue = modelValue;

				IBindingConverter<?, ?> converter = m_converter;
				if(null != converter) {
					modelValue = ((IBindingConverter<Object, Object>) converter).modelToControl(modelValue);
				}

				if(m_controlProperty.getReadOnly() != YesNoType.YES) {
					((IValueAccessor<Object>) m_controlProperty).setValue(m_control, modelValue);
				}
				m_bindError = null;                                    // Let's assume binding has no trouble.
			}
		} catch(Exception x) {
			throw new BindingFailureException(x, "Model->Control", this.toString());
		}
	}

}
