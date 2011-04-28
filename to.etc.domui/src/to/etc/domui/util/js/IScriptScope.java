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

public interface IScriptScope {
	/**
	 * Return the value for the specified variable in this scope.
	 * @param name
	 * @return
	 */
	Object getValue(String name);

	/**
	 * Put a value inside the scope.
	 * @param name
	 * @param instance
	 */
	void put(String name, Object instance);

	void registerToplevelFunction(Object instance, String instanceVar, String function) throws Exception;

	/**
	* Create a new writable scope that has this scope as the "delegate". This new scope
	* is writable.
	* @return
	*/
	IScriptScope newScope();

	<T> T getAdapter(Class<T> clz);
}
