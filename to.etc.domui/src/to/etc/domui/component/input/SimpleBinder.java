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
import to.etc.domui.dom.html.*;
import to.etc.domui.logic.events.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;

/**
 * This is a single binding instance between a control and one of the control's properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public class SimpleBinder implements IBinder, ILogiEventListener {
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
	 */
	void moveControlToModel() throws Exception {
		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveControlToModel((NodeBase) m_control);
			return;
		}
		PropertyMetaModel< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");

		try {
			Object value = m_controlProperty.getValue(m_control);
			((PropertyMetaModel<Object>) instanceProperty).setValue(m_instance, value);
		} catch(Exception x) {
			System.out.println("Binding error moving " + m_controlProperty + " to " + m_instanceProperty + ": " + x);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	void moveModelToControl() throws Exception {
		IBindingListener< ? > listener = m_listener;
		if(listener != null) {
			((IBindingListener<NodeBase>) listener).moveModelToControl((NodeBase) m_control);
			return;
		}
		PropertyMetaModel< ? > instanceProperty = m_instanceProperty;
		if(null == instanceProperty)
			throw new IllegalStateException("instance property cannot be null");
		Object base = m_instance;
		if(base != null) {
			// FIXME We should think about exception handling here
			Object val = instanceProperty.getValue(base);
			((PropertyMetaModel<Object>) m_controlProperty).setValue(m_control, val);
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

	/**
	 * Handle a logic event: if the event contains a change to something we're bound to then
	 * update that thing we're bound to.
	 * @see to.etc.domui.logic.events.ILogiEventListener#logicEvent(to.etc.domui.logic.events.LogiEvent)
	 */
	@Deprecated
	@Override
	public void logicEvent(@Nonnull LogiEvent event) throws Exception {
		//-- If I just have a listener pass on the event to the listener.
		IBindingListener< ? > listener = m_listener;
		if(null != listener) {
			if(listener instanceof ILogiEventListener) {
				((ILogiEventListener) listener).logicEvent(event);
			}
			return;
		}

		//-- Get my binding as instance:property.
		Object base = getBase();
		if(!event.propertyChanged(base, pmm().getName()))
			return;

		//-- The thing we're bound to has changed value. For now just set the new value.
		moveModelToControl();
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
