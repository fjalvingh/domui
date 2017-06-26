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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.domui.util.DomUtil.IPerNode;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * This is a single binding instance between a control and one of the control's properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
final public class SimpleBinder implements IBinder, IBinding {
	static public final String BINDING_ERROR = "BindingError";

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
	private Object m_lastValueFromControl;

	/** If this binding is in error this contains the error. */
	@Nullable
	private UIMessage m_bindError;

	final static private Map<Class<?>, Class<?>> BOXINGDISASTER = new HashMap<>();

	static {
		BOXINGDISASTER.put(long.class, Long.class);
		BOXINGDISASTER.put(int.class, Integer.class);
		BOXINGDISASTER.put(short.class, Short.class);
		BOXINGDISASTER.put(char.class, Character.class);
		BOXINGDISASTER.put(double.class, Double.class);
		BOXINGDISASTER.put(float.class, Float.class);
		BOXINGDISASTER.put(boolean.class, Boolean.class);
		BOXINGDISASTER.put(byte.class, Byte.class);
	}

	public SimpleBinder(@Nonnull NodeBase control, @Nonnull String controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		m_control = control;
		m_controlProperty = MetaManager.getPropertyMeta(control.getClass(), controlProperty);
	}

	private void checkAssigned() {
		if(m_listener != null || m_instance != null)
			throw new ProgrammerErrorException("This binding is already fully defined. Create a new one.");
	}

	@Override
	public void to(@Nonnull IBindingListener< ? > listener) {
		checkAssigned();
		if(listener == null)
			throw new IllegalArgumentException("Argument cannot be null");
		m_listener = listener;
	}

	@Override
	public <T> void to(@Nonnull T instance, @Nonnull String property) throws Exception {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Bind to a IValueAccessor and the given instance.
	 * @param instance
	 * @param pmm
	 * @throws Exception
	 */
	@Override
	public <T, V> void to(@Nonnull T instance, @Nonnull IValueAccessor<V> pmm) throws Exception {
		checkAssigned();
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");
		m_instanceProperty = pmm;
		m_instance = instance;

		//-- Check: are the types of the binding ok?
		if(pmm instanceof PropertyMetaModel<?>) {
			PropertyMetaModel<?> p = (PropertyMetaModel<?>) pmm;
			Class<?> actualType = fixBoxingDisaster(p.getActualType());
			Class<?> controlType = fixBoxingDisaster(m_controlProperty.getActualType());

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

	@Nonnull
	static private Class<?> fixBoxingDisaster(@Nonnull Class<?> clz) {
		Class<?> newClass = BOXINGDISASTER.get(clz);
		return newClass != null ? newClass : clz;
	}

	/**
	 * If this binding is in error: return the message describing that error.
	 * @return
	 */
	@Override
	@Nullable
	public UIMessage getBindError() {
		return m_bindError;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IModelBinding interface implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Move the control value to wherever it's needed. If this is a listener binding it calls the listener,
	 * else it moves the value either to the model's value or the instance's value.
	 *
	 * This is the *hard* part of binding: it needs to handle control errors caused by bindValue() throwing
	 * an exception.
	 */
	@Override
	public void moveControlToModel() throws Exception {
		NodeBase control = m_control;
		if(control instanceof IDisplayControl)
			return;


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
				return;
			}
		}


		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveControlToModel(control);
			return;
		}

		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
		if(instanceProperty.isReadOnly())
			return;
		Object instance = m_instance;
		if(null == instance)
			throw new IllegalStateException("instance cannot be null");

		/*
		 * Get the control's value. If the control is in error (validation/conversion) then
		 * add the problem inside the Error collector, signaling a problem to any logic
		 * that would run after.
		 */
		Object value = null;
		UIMessage newError = null;
		try {
			value = m_controlProperty.getValue(m_control);
			m_lastValueFromControl = value;
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
		if(null == newError) {
			//-- QUESTION: Should we move something to the model @ error?
			try {
				((IValueAccessor<Object>) instanceProperty).setValue(instance, value);
			} catch(Exception x) {
				throw new IllegalStateException("Binding error moving " + m_controlProperty + " to " + m_instanceProperty + ": " + x, x);
			}
		}
		//System.out.println("binder: get " + m_control.getComponentInfo() + " value -> model " + value);
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
			IValueAccessor<?> instanceProperty = m_instanceProperty;
			if(null == instanceProperty)
				throw new IllegalStateException("instance property cannot be null");
			Object instance = m_instance;
			if(null == instance)
				throw new IllegalStateException("instance cannot be null");
			NodeBase control = m_control;

			// FIXME We should think about exception handling here
			Object modelValue = instanceProperty.getValue(instance);
			//System.out.println("binder: set "+control.getComponentInfo()+" value="+modelValue);
			if(!MetaManager.areObjectsEqual(modelValue, m_lastValueFromControl)) {
				//-- Value in instance differs from control's
				m_lastValueFromControl = modelValue;
				((IValueAccessor<Object>) m_controlProperty).setValue(m_control, modelValue);
				m_bindError = null;                                    // Let's assume binding has no trouble.

//			/*
//			 * jal yeah, this also suffers from "knowing" that we're accessing the control's value 8-/ We need
//			 * to think about this.
//			 *
//			 * When updated from the model: clear the control's error. This should probably call the control's validation
//			 * methods to see if the new value set obeys the control's validation (notably: mandatoryness, pattern).
//			 */
//			if(control.isAttached()) {
//				UIMessage ctlError = control.getMessage();
//				if(null != ctlError) {
//					LogiErrors errorModel = control.lc().getErrorModel();
//					errorModel.clearMessage(instance, instanceProperty, ctlError);
//					control.setMessage(null);
//				}
//			}
			} else {
//			/*
//			 * Model has not updated the value. If the control *itself* has an error (Which can be known because the
//			 * last bind error is != null) we keep that error, otherwise we set the 1st error from the model.
//			 */
//			if(m_bindError == null) {
//				if(control.isAttached()) {
//					LogiErrors errorModel = control.lc().getErrorModel();
//					Set<UIMessage> e2b = errorModel.getErrorsOn(instance, instanceProperty);
//					UIMessage msg = null;
//					if(e2b.size() > 0) {
//						msg = e2b.iterator().next();
//					}
//					control.setMessage(msg);
//				}
//			}
			}
		} catch(Exception x) {
			throw new BindingFailureException(x, "Model->Control", this.toString());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling simple binding chores.						*/
	/*--------------------------------------------------------------*/
	/**
	 * System helper method to move all bindings from control into the model (called at request start).
	 * @param root
	 * @throws Exception
	 */
	static public void controlToModel(@Nonnull NodeBase root) throws Exception {
		DomUtil.walkTreeUndelegated(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list)
						sb.moveControlToModel();
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
	}

	/**
	 * System helper method to move all bindings from model to control (called at request end).
	 * @param root
	 * @throws Exception
	 */
	static public void modelToControl(@Nonnull NodeBase root) throws Exception {
		DomUtil.walkTreeUndelegated(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list)
						sb.moveModelToControl();
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
	}

	/**
	 * Get a list of binding errors starting at (and including) the parameter node. Each
	 * message will contain the NodeBase control that failed inside {@link UIMessage#getErrorNode()}.
	 * @param root
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public List<UIMessage> getBindingErrors(@Nonnull NodeBase root) throws Exception {
		final List<UIMessage> res = new ArrayList<>();
		DomUtil.walkTreeUndelegated(root, new IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list) {
						UIMessage message = sb.getBindError();
						if(null != message)
							res.add(message);
					}
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
		return res;
	}

	/**
	 * If the specified subtree has binding errors: report them, and return TRUE if there are
	 * errors.
	 * @param root
	 * @return true if errors are present
	 * @throws Exception
	 */
	static public boolean reportBindingErrors(@Nonnull NodeBase root) throws Exception {
		final boolean[] silly = new boolean[1];					// Not having free variables is a joke.
		DomUtil.walkTreeUndelegated(root, new IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					List<UIMessage> bindErrorList= new ArrayList<>();

					//-- Find all bindings with an error
					for(IBinding sb : list) {
						UIMessage message = sb.getBindError();
						if(null != message) {
							bindErrorList.add(message);
						}
					}

					//-- If there is an error somewhere- report the 1st one on the component
					if(bindErrorList.size() > 0) {
						UIMessage message = bindErrorList.get(0);		// Report the first error as the binding error.
						message.group(BINDING_ERROR);
						silly[0] = true;
						n.setMessage(message);
					} else {
						/*
						 * jal 20160215 This binding's component does not have a binding error now. An old
						 * comment said "should not be reset: should be done by component itself". That seems
						 * to be wrong, though. We should not just set the component error to null here, because
						 * an error can be put there by something else. But if the component is showing a binding
						 * error caused by a /previous/ run of this code then that error should be removed, because
						 * otherwise no one does! The component cannot do it because it is forbidden to play
						 * with messages during binding.
						 */
						UIMessage componentMessage = n.getMessage();
						if(componentMessage != null && BINDING_ERROR.equals(componentMessage.getGroup())) {
							n.setMessage(null);
						}
					}
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
		return silly[0];
	}

	@Nullable
	public static SimpleBinder findBinding(NodeBase nodeBase, String string) {
		List<IBinding> list = nodeBase.getBindingList();
		if(list != null) {
			for(IBinding sb : list) {
				if(sb instanceof SimpleBinder) {
					SimpleBinder sib = (SimpleBinder) sb;
					IValueAccessor<?> property = sib.getControlProperty();
					if(property instanceof PropertyMetaModel) {
						if(string.equals(((PropertyMetaModel<?>) property).getName()))
							return sib;
					}
				}
			}
		}
		return null;
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
}
