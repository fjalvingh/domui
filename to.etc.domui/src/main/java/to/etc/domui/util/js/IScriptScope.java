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
package to.etc.domui.util.js;

import java.io.*;
import java.util.*;

import javax.annotation.*;

public interface IScriptScope {
	/**
	 * Return the value for the specified variable in this scope. If the specified variable
	 * cannot be converted to the required type this throws an exception.
	 * @param name
	 * @return
	 */
	@Nullable
	<T> T getValue(@Nonnull Class<T> valueClass, @Nonnull String name);

	/**
	 * Put a simple value inside the scope.
	 * @param name
	 * @param instance
	 */
	<T> void put(@Nonnull String name, @Nullable T instance);

	/**
	 * Get all properties of this object.
	 * @param filterClass
	 * @return
	 */
	@Nonnull
	<T> List<T> getProperties(@Nonnull Class<T> filterClass);

	/**
	 * Create a new object property inside this one, with the specified name.
	 * @param name
	 * @return
	 */
	@Nonnull
	IScriptScope addObjectProperty(@Nonnull String name);

	@Nullable
	<T> T eval(@Nonnull Class<T> targetType, @Nonnull Reader r, @Nonnull String sourceFileNameIndicator) throws Exception;

	@Nullable
	<T> T eval(@Nonnull Class<T> targetType, @Nonnull String expression, @Nonnull String sourceFileNameIndicator) throws Exception;

	/**
	* Create a new writable scope that has this scope as the "delegate". This new scope
	* is writable.
	* @return
	*/
	@Nonnull
	IScriptScope newScope();

	@Nullable
	<T> T getAdapter(@Nonnull Class<T> clz);
}
