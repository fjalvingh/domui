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
package to.etc.iocular.def;

/**
 * Defines the basic "instance creation" method as defined by a builder for a component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 15, 2009
 */
public enum CreateMethod {
	/**
	 * Create the object by doing a 'new' on the object. It will use the most optimal constructor using
	 * a greedy algorithm (i,e, it uses the constructor with the most or most specialized parameters).
	 */
	ASNEW,

	/**
	 * The object is a container parameter; it's value will be set at runtime when the container is constructed.
	 */
	CONTAINER_PARAMETER,

	/**
	 * Created by some factory, by calling a method on a static or known instance.
	 */
	FACTORY_METHOD,


}
