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

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Nodes that can accept dropped things must have this interface defined via setDropHandler(). The instance
 * of this handler determines what happens with the dropped node, and which nodes are acceptable for dropping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public interface IDropHandler {
	/**
	 * Returns the list of types acceptable for this target. Only draggable's that have
	 * one of these types will be accepted. This MUST return a string array containing at
	 * least one string; if not an exception occurs as soon as this gets used.
	 * @return	a non-null minimal length=1 String array containing the types that are acceptable for this drop zone.
	 */
	@Nonnull String[] getAcceptableTypes();

	/**
	 * This is an event function which gets called as soon as a Draggable is dropped on the dropTarget
	 * having this handler. This event gets called <i>after</i> IDragHandler.onDropped() has been called
	 * for the dropped draggable.
	 *
	 * @param context
	 * @throws Exception
	 */
	void onDropped(@Nonnull DropEvent context) throws Exception;

	/**
	 * Returns drag&drop mode that is supported by drop handler.
	 */
	@Nonnull DropMode getDragMode();

}
