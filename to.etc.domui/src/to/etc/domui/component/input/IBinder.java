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

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * A thingy which handles binding a control to a model/property, data move event or
 * IReadOnlyModel/property.
 *
 * FIXME Question - should this <i>be</i> a IModelBinding or should this <i>have</i> a IModelBinding?
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBinder extends IModelBinding {
	/**
	 * Create a binding to the associated control and the specified object instance and the named property of that instance.
	 * @param instance
	 * @param property
	 */
	void to(@Nonnull Object instance, @Nonnull String property);

	/**
	 * Create a binding to the associated instance's property whose metadata is passed.
	 * @param instance
	 * @param pmm
	 */
	void to(@Nonnull Object instance, @Nonnull PropertyMetaModel< ? > pmm);

	/**
	 * Create a binding between the associated control, the specified model and the property specified.
	 * @param <T>
	 * @param theClass
	 * @param model
	 * @param property
	 */
	<T> void to(@Nonnull Class<T> theClass, @Nonnull IReadOnlyModel<T> model, @Nonnull String property);

	/**
	 * Create a binding between the specified model and the property whose metadata is passed in.
	 * @param <T>
	 * @param model		The model to obtain an instance from
	 * @param pmm		The propertymeta for a property on that instance.
	 */
	<T> void to(@Nonnull IReadOnlyModel<T> model, @Nonnull PropertyMetaModel< ? > pmm);

	/**
	 * Bind the control to a listener.
	 * @param listener
	 */
	void to(@Nonnull IBindingListener< ? > listener);

	/**
	 * If this object is actually bound to something return true.
	 *
	 * @return
	 */
	boolean isBound();

}
