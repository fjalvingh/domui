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
import to.etc.domui.logic.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * This is a single binding instance between a control and one of the control's properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public class SimpleBinder implements IBinder {
	@Nonnull
	final private IBindable m_control;

	@Nonnull
	final private PropertyMetaModel< ? > m_controlProperty;

	/** The instance bound to */
	@Nullable
	private Object m_instance;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	@Nullable
	private PropertyMetaModel< ? > m_instanceProperty;

	/** If this thing is bound to some event listener... */
	@Nullable
	private IBindingListener< ? > m_listener;

	@Nullable
	private Object m_lastBindValue;

	@Nullable
	private UIMessage m_lastBindError;

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
	 * Bind to a propertyMetaModel and the given instance.
	 * @param instance
	 * @param pmm
	 * @throws Exception
	 */
	@Override
	public <T, V> void to(@Nonnull T instance, @Nonnull PropertyMetaModel<V> pmm) throws Exception {
		checkAssigned();
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");
		m_instanceProperty = pmm;
		m_instance = instance;

		//-- Move the data now!
		moveModelToControl();
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
		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveControlToModel((NodeBase) m_control);
			return;
		}

		PropertyMetaModel< ? > instanceProperty = m_instanceProperty;
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
		try {
			value = m_controlProperty.getValue(m_control);
		} catch(CodeException cx) {
			//-- Conversion/validation or other UI related trouble.
/*			actually, we do nothing here -> we read error set on control anyway...
 *
 * 			UIMessage err = UIMessage.error(cx);
			LogiErrors errorModel = ((NodeBase) m_control).lc().getErrorModel();
			errorModel.message(instance, instanceProperty, err);
*/

		} // throw all others

		LogiErrors errorModel = ((NodeBase) m_control).lc().getErrorModel();
		UIMessage err = ((NodeBase) m_control).getMessage();
		if(err != null) {
			errorModel.message(instance, instanceProperty, err);
		} else {
			errorModel.clearMessages(instance, instanceProperty);
		}

		//-- QUESTION: Should we move something to the model @ error?
		try {
			m_lastBindValue = value;
			m_lastBindError = err;
			((PropertyMetaModel<Object>) instanceProperty).setValue(m_instance, value);
		} catch(Exception x) {
			System.out.println("Binding error moving " + m_controlProperty + " to " + m_instanceProperty + ": " + x);
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
		PropertyMetaModel< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
		Object instance = m_instance;
		if(instance != null) {
			// FIXME We should think about exception handling here
			Object modelValue = instanceProperty.getValue(instance);
			if(!MetaManager.areObjectsEqual(modelValue, m_lastBindValue)) {
				m_lastBindValue = modelValue;
				((PropertyMetaModel<Object>) m_controlProperty).setValue(m_control, modelValue);
			}
			LogiErrors errorModel = ((NodeBase) m_control).lc().getErrorModel();
			UIMessage errorToBind = null;
			List<UIMessage> errorsToBind = errorModel.getErrorsOn(instance, instanceProperty);
			if(!errorsToBind.isEmpty()) {
				errorToBind = errorsToBind.get(0);
				if(!errorToBind.equals(m_lastBindError)) {
					//set first error from error model...
					((NodeBase) m_control).setMessage(errorToBind);
					m_lastBindError = errorToBind;
				}
			} else {
				((NodeBase) m_control).setMessage(null);
				m_lastBindError = null;
			}
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

	@Nullable
	public static SimpleBinder findBinding(NodeBase nodeBase, String string) {
		if(nodeBase instanceof IBindable) {
			IBindable b = (IBindable) nodeBase;
			List<SimpleBinder> list = b.getBindingList();
			if(list != null) {
				for(SimpleBinder sb : list) {
					if(string.equals(sb.getControlProperty().getName()))
						return sb;
				}
			}
		}
		return null;
	}

	@Nonnull
	public PropertyMetaModel< ? > getControlProperty() {
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
		PropertyMetaModel< ? > instanceProperty = m_instanceProperty;
		if(instanceProperty != null) {
			sb.append("/").append(instanceProperty.getName());
		}
		sb.append("]");
		return sb.toString();
	}
}
