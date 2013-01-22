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

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * This is a simple binder implementation for base IControl<T> implementing controls. It handles all
 * binding chores.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public class SimpleBinder implements IBinder {
	@Nonnull
	private IControl< ? > m_control;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	@Nullable
	private PropertyMetaModel< ? > m_propertyModel;

	/** If this is bound to some model this contains the model, */
	@Nullable
	private IReadOnlyModel< ? > m_model;

	/** If this is bound to an object instance directly it contains the instance */
	@Nullable
	private Object m_instance;

	/** If this thing is bound to some event listener... */
	@Nullable
	private IBindingListener< ? > m_listener;

	public SimpleBinder(@Nonnull IControl< ? > control) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		m_control = control;
	}

	/**
	 * Returns T if this contains an actual binding. We are bound if property is set OR a listener is set.
	 * @see to.etc.domui.component.input.IBinder#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_propertyModel != null || m_listener != null;
	}

	/**
	 * Bind to a property of the object returned by this model.
	 * @see to.etc.domui.component.input.IBinder#to(java.lang.Class, to.etc.domui.util.IReadOnlyModel, java.lang.String)
	 */
	@Override
	public <T> void to(@Nonnull Class<T> theClass, @Nonnull IReadOnlyModel<T> model, @Nonnull String property) {
		if(theClass == null || property == null || model == null)
			throw new IllegalArgumentException("Argument cannot be null");
		m_listener = null;
		m_propertyModel = MetaManager.getPropertyMeta(theClass, property);
		m_model = model;
		m_instance = null;
	}

	/**
	 * Bind to a property on some model whose metadata is passed.
	 * @param <T>
	 * @param model
	 * @param pmm
	 */
	@Override
	public <T> void to(@Nonnull IReadOnlyModel<T> model, @Nonnull PropertyMetaModel< ? > pmm) {
		if(pmm == null || model == null)
			throw new IllegalArgumentException("Argument cannot be null");
		m_listener = null;
		m_propertyModel = pmm;
		m_model = model;
		m_instance = null;
	}

	/**
	 *
	 * @see to.etc.domui.component.input.IBinder#to(to.etc.domui.component.input.IBindingListener)
	 */
	@Override
	public void to(@Nonnull IBindingListener< ? > listener) {
		if(listener == null)
			throw new IllegalArgumentException("Argument cannot be null");
		m_propertyModel = null;
		m_instance = null;
		m_model = null;
		m_listener = listener;
	}

	/**
	 * Bind to a property of the instance specified.
	 *
	 * @see to.etc.domui.component.input.IBinder#to(java.lang.Object, java.lang.String)
	 */
	@Override
	public void to(@Nonnull Object instance, @Nonnull String property) {
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
	public void to(@Nonnull Object instance, @Nonnull PropertyMetaModel< ? > pmm) {
		if(instance == null || pmm == null)
			throw new IllegalArgumentException("Parameters in a bind request CANNOT be null!");
		m_listener = null;
		m_model = null;
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
			if(m_propertyModel.getReadOnly() == YesNoType.YES)
				return;
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

	@Nonnull
	private IReadOnlyModel< ? > getModel() {
		if(null != m_model)
			return m_model;
		throw new IllegalStateException("The model cannot be null");
	}

	@Override
	public void setControlsEnabled(boolean on) {
		m_control.setDisabled(!on);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(m_instance != null) {
			sb.append("i=").append(m_instance);
		} else if(m_listener != null) {
			sb.append("l=").append(m_listener);
		} else if(m_model != null) {
			sb.append("m=").append(m_model);
		} else {
			sb.append("?");
		}
		if(m_propertyModel != null) {
			sb.append("/").append(m_propertyModel.getName());
		}
		return sb.toString();
	}
}
