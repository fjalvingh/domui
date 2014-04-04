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
import to.etc.domui.util.*;
import to.etc.webapp.*;

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
	final private String m_controlProperty;

	/** The instance bound to */
	@Nullable
	private Object m_instance;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	@Nullable
	private PropertyMetaModel< ? > m_propertyModel;

	/** If this thing is bound to some event listener... */
	@Nullable
	private IBindingListener< ? > m_listener;

	public SimpleBinder(@Nonnull IBindable control, @Nonnull String controlProperty) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		m_control = control;
		m_controlProperty = controlProperty;
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
	public <T> void to(@Nonnull T instance, @Nonnull String property) {
		if(instance == null || property == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		to(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
	}

	/**
	 * Bind to a propertyMetaModel and the given instance.
	 * @param instance
	 * @param pmm
	 */
	@Override
	public <T, V> void to(@Nonnull T instance, @Nonnull PropertyMetaModel<V> pmm) {
		checkAssigned();
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");
		m_propertyModel = pmm;
		m_instance = instance;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IModelBinding interface implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Move the control value to wherever it's needed. If this is a listener binding it calls the listener,
	 * else it moves the value either to the model's value or the instance's value.
	 * @see to.etc.domui.component.controlfactory.IModelBinding#moveControlToModel()
	 */
	@Override
	public void moveControlToModel() throws Exception {
		if(m_listener != null)
			((IBindingListener<NodeBase>) m_listener).moveControlToModel((NodeBase) m_control); // Stupid generics idiocy requires cast
		else {
			Object val = m_control.getValue();
			Object base = m_instance == null ? getModel().getValue() : m_instance;
			IValueAccessor<Object> a = (IValueAccessor<Object>) m_propertyModel;
			if(null == a)
				throw new IllegalStateException("The propertyModel cannot be null");
			a.setValue(base, val);
		}
	}

	@Override
	public void moveModelToControl() throws Exception {
		if(m_listener != null)
			((IBindingListener<NodeBase>) m_listener).moveModelToControl((NodeBase) m_control); // Stupid generics idiocy requires cast
		else {
			Object base = m_instance == null ? getModel().getValue() : m_instance;
			IValueAccessor< ? > vac = m_propertyModel;
			if(vac == null)
				throw new IllegalStateException("Null IValueAccessor<T> returned by PropertyMeta " + m_propertyModel);
			Object pval = vac.getValue(base);
			((IControl<Object>) m_control).setValue(pval);
		}
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
		if(m_propertyModel != null) {
			sb.append("/").append(m_propertyModel.getName());
		}
		sb.append("]");
		return sb.toString();
	}

	@Nullable
	public static SimpleBinder findBinding(NodeBase nodeBase, String string) {
		if(nodeBase  instanceof IBindable) {
			IBindable b = (IBindable) nodeBase;
			List<SimpleBinder> list = b.getBindingList();
			if(list != null) {
				for(SimpleBinder sb: list) {
					if(string.equals(sb.getControlProperty()))
						return sb;
				}
			}
		}
		return null;
	}

	@Nonnull
	public String getControlProperty() {
		return m_controlProperty;
	}
}
