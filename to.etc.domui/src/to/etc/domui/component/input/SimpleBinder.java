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
package to.etc.domui.component.input;

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
final public class SimpleBinder implements IBinder {
	@Nonnull
	final private IBindable m_control;

	@Nonnull
	final private IValueAccessor< ? > m_controlProperty;

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

	public SimpleBinder(@Nonnull IBindable control, @Nonnull String controlProperty) {
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

		//-- Move the data now!
		moveModelToControl();
	}

	/**
	 * If this binding is in error: return the message describing that error.
	 * @return
	 */
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
	public void moveControlToModel() throws Exception {
		NodeBase control = (NodeBase) m_control;
		if(control instanceof IDisplayControl)
			return;

		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveControlToModel(control);
			return;
		}

		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
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
		} catch(CodeException cx) {
			newError = UIMessage.error(cx);
			newError.setErrorNode(control);
			newError.setErrorLocation(control.getErrorLocation());
			System.out.println("~~ " + control + " to " + instanceProperty + ": " + cx);
		}
		m_bindError = newError;
		if(null == newError) {
			//-- QUESTION: Should we move something to the model @ error?
			try {
				((IValueAccessor<Object>) instanceProperty).setValue(instance, value);
			} catch(Exception x) {
				throw new IllegalStateException("Binding error moving " + m_controlProperty + " to " + m_instanceProperty + ": " + x, x);
			}
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void moveModelToControl() throws Exception {
		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveModelToControl((NodeBase) m_control);
			return;
		}
		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
		Object instance = m_instance;
		if(null == instance)
			throw new IllegalStateException("instance cannot be null");
		NodeBase control = (NodeBase) m_control;

		// FIXME We should think about exception handling here
		Object modelValue = instanceProperty.getValue(instance);
		if(!MetaManager.areObjectsEqual(modelValue, m_lastValueFromControl)) {
			//-- Value in instance differs from control's
			m_lastValueFromControl = modelValue;
			((IValueAccessor<Object>) m_controlProperty).setValue(m_control, modelValue);
			m_bindError = null;									// Let's assume binding has no trouble.

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
		DomUtil.walkTree(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					IBindable b = (IBindable) n;
					List<SimpleBinder> list = b.getBindingList();
					if(null != list) {
						for(SimpleBinder sb : list)
							sb.moveControlToModel();
					}
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
		DomUtil.walkTree(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					IBindable b = (IBindable) n;
					List<SimpleBinder> list = b.getBindingList();
					if(null != list) {
						for(SimpleBinder sb : list)
							sb.moveModelToControl();
					}
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
		DomUtil.walkTree(root, new IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					IBindable b = (IBindable) n;
					List<SimpleBinder> list = b.getBindingList();
					if(null != list) {
						for(SimpleBinder sb : list) {
							UIMessage message = sb.getBindError();
							if(null != message)
								res.add(message);
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
		return res;
	}

	static public boolean reportBindingErrors(@Nonnull NodeBase root) throws Exception {
		final boolean[] silly = new boolean[1];					// Not having free variables is a joke.
		DomUtil.walkTree(root, new IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					IBindable b = (IBindable) n;
					List<SimpleBinder> list = b.getBindingList();
					if(null != list) {
						for(SimpleBinder sb : list) {
							UIMessage message = sb.getBindError();
							if(null != message) {
								silly[0] = true;
								n.setMessage(message);
							} else {
//								n.setMessage(null);				// Should not be reset: should be done by component itself
							}
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
		if(nodeBase instanceof IBindable) {
			IBindable b = (IBindable) nodeBase;
			List<SimpleBinder> list = b.getBindingList();
			if(list != null) {
				for(SimpleBinder sb : list) {
					IValueAccessor<?> property = sb.getControlProperty();
					if(property instanceof PropertyMetaModel) {
						if(string.equals(((PropertyMetaModel<?>) property).getName()))
							return sb;
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
			sb.append("i=").append(m_instance);
		} else if(m_listener != null) {
			sb.append("l=").append(m_listener);
		} else {
			sb.append("?");
		}
		IValueAccessor< ? > instanceProperty = m_instanceProperty;
		if(instanceProperty != null) {
			sb.append("/");
			if(instanceProperty instanceof PropertyMetaModel) {
				sb.append(((PropertyMetaModel< ? >) instanceProperty).getName());
			} else {
				sb.append(instanceProperty.toString());
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
