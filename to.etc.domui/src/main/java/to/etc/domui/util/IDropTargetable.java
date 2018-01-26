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
package to.etc.domui.util;

/**
 * This interface is present on node types that have the possibility to
 * be used as drag 'n drop <i>drop targets</i>, i.e. that can receive a
 * node being dragged. This is present on specific HTML DOM nodes only.
 * The interface itself does not indicate that dropping is allowed; it
 * only exposes the setter and getter for an IDropHandler interface. Only
 * when an instance of this interface is set on the node implementing this
 * interface will it be able to accept dropped nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public interface IDropTargetable {
	/**
	 * Make this node acceptable for dropping dragged items into. The handler specified handles
	 * the actual drop events and drop accept events. When set to null this node will no longer
	 * accept dropped thingerydoo's.
	 *
	 * @param handler
	 */
	void setDropHandler(IDropHandler handler);

	/**
	 * Return the current drop handler for a node. If null the node does not accept dropped
	 * thingerydoo's.
	 * @return
	 */
	IDropHandler getDropHandler();
}
