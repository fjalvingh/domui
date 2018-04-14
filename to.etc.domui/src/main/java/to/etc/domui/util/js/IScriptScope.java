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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Reader;
import java.util.List;

public interface IScriptScope {
	/**
	 * Return the value for the specified variable in this scope. If the specified variable
	 * cannot be converted to the required type this throws an exception.
	 * @param name
	 * @return
	 */
	@Nullable
	<T> T getValue(@NonNull Class<T> valueClass, @NonNull String name);

	/**
	 * Put a simple value inside the scope.
	 * @param name
	 * @param instance
	 */
	<T> void put(@NonNull String name, @Nullable T instance);

	/**
	 * Get all properties of this object.
	 * @param filterClass
	 * @return
	 */
	@NonNull
	<T> List<T> getProperties(@NonNull Class<T> filterClass);

	/**
	 * Create a new object property inside this one, with the specified name.
	 * @param name
	 * @return
	 */
	@NonNull
	IScriptScope addObjectProperty(@NonNull String name);

	@Nullable
	<T> T eval(@NonNull Class<T> targetType, @NonNull Reader r, @NonNull String sourceFileNameIndicator) throws Exception;

	@Nullable
	<T> T eval(@NonNull Class<T> targetType, @NonNull String expression, @NonNull String sourceFileNameIndicator) throws Exception;

	/**
	* Create a new writable scope that has this scope as the "delegate". This new scope
	* is writable.
	* @return
	*/
	@NonNull
	IScriptScope newScope();

	@Nullable
	<T> T getAdapter(@NonNull Class<T> clz);
}
