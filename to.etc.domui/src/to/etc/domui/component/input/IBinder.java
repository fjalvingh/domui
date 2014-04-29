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
import to.etc.webapp.annotations.*;

/**
 * This exposes the several kinds of bindings that can be done on any control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBinder {
	/**
	 * Creates a "value" property binding from the control TO the model object's property specified. If the
	 * control does not <b>have</i> a value property this throws an exception.
	 * @param instance
	 * @param property
	 */
	public <T> void to(@Nonnull T instance, @Nonnull @GProperty String property) throws Exception;

	/**
	 * Creates a "value" property binding from the control TO the model object's property specified. If the
	 * control does not <b>have</i> a value property this throws an exception.
	 *
	 * @param instance
	 * @param pmm
	 */
	public <T, V> void to(@Nonnull T instance, @Nonnull PropertyMetaModel<V> pmm) throws Exception;

	/**
	 * Bind the control to a listener.
	 * @param listener
	 */
	public void to(@Nonnull IBindingListener< ? > listener);
}
