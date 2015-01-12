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
package to.etc.domui.component.controlfactory;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

/**
 * EXPERIMENTAL INTERFACE
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 2, 2011
 */
public final class BindingMessenger {
	@Nonnull
	private ModelBindings m_bindings;

	@Nullable
	private BundleRef m_bundleRef;

	@Nullable
	private Object m_object;

	public BindingMessenger(@Nonnull ModelBindings bindings, @Nonnull BundleRef bundleRef) {
		m_bindings = bindings;
		m_bundleRef = bundleRef;
	}

	/**
	 * Resolves the classbundle for the Urlpage
	 * @param object
	 * @param bindings
	 * @param urlClass
	 */
	public BindingMessenger(@Nonnull Object object, @Nonnull ModelBindings bindings, @Nonnull Class< ? extends UrlPage> urlClass) {
		this(object, bindings, MetaManager.findClassMeta(urlClass).getClassBundle());
	}

	public BindingMessenger(@Nonnull Object object, @Nonnull ModelBindings bindings, @Nonnull BundleRef bundleRef) {
		m_bindings = bindings;
		m_bundleRef = bundleRef;
		m_object = object;
	}

	public BindingMessenger(@Nonnull ModelBindings bindings) {
		m_bindings = bindings;
	}

	public BindingMessenger(@Nonnull Object object, @Nonnull ModelBindings bindings) {
		super();
		m_bindings = bindings;
		m_object = object;
	}

	public void error(@Nonnull Object object, @Nonnull String property, @Nonnull String message, Object... param) throws Exception {
		error(object, property, UIMessage.error(DomUtil.nullChecked(m_bundleRef), message, param));
	}

	@Nonnull
	public Object getObject() {
		if(null != m_object)
			return m_object;
		throw new IllegalStateException("This binding does not have an object set.");
	}

	public void error(@Nonnull String property, @Nonnull String message, @Nullable Object... param) throws Exception {
		BundleRef bundleRef = DomUtil.nullChecked(m_bundleRef);
		error(getObject(), property, UIMessage.error(bundleRef, message, param));
	}

	/**
	 * Sends a message to the control that belongs to the object and property
	 * @param object
	 * @param property
	 * @param message
	 * @throws Exception
	 */
	public void error(@Nonnull Object object, @Nonnull String property, @Nonnull UIMessage m) throws Exception {
		IControl< ? >[] h = new IControl[1];
		find(m_bindings, h, object, property);
		if(h[0] == null) {
			throw new ProgrammerErrorException(object.getClass().getSimpleName() + "." + property + " not found in bindings"); // FIXME This should cause the message to occur as global message.
		}
		h[0].setMessage(m);
	}

	private void find(@Nonnull ModelBindings bindings, @Nonnull IControl< ? >[] h, @Nonnull Object object, @Nonnull String property) throws Exception {
		for(IModelBinding mb : bindings) {
			if(mb instanceof SimpleComponentPropertyBinding) {
				SimpleComponentPropertyBinding< ? > b = (SimpleComponentPropertyBinding< ? >) mb;
				if(b.getModel().getValue().equals(object) && b.getPropertyMeta().getName().equals(property)) {
					h[0] = b.getControl();
					return;
				}
			} else if(mb instanceof ModelBindings) {
				ModelBindings modelBindings = (ModelBindings) mb;
				find(modelBindings, h, object, property);
			}
		}
	}

	/**
	 * Tries to find the control in the binding belong to the object and property
	 * @param object
	 * @param property
	 * @throws Exception
	 */
	public <T> IControl<T> findControl(@Nonnull String property) throws Exception {
		return findControl(getObject(), property);
	}

	public <T> IControl<T> findControl(@Nonnull Object object, @Nonnull String property) throws Exception {
		IControl< ? >[] h = new IControl[1];
		find(m_bindings, h, object, property);
		if(h[0] == null) {
			throw new ProgrammerErrorException(object.getClass().getSimpleName() + "." + property + " not found in bindings"); // FIXME This should cause the message to occur as global message.
		}
		return (IControl<T>) h[0];
	}
}
