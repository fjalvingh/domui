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

public interface IDragHandler {
	/**
	 * This must return a "type name" for the thing being dragged. This typename gets passed to
	 * any "drop target" and allows that to indicate whether that type is acceptable for that
	 * drop target.
	 * @return a non-null string.
	 */
	@Nonnull
	String getTypeName(@Nonnull NodeBase source);

	/**
	 * Called when the dragged node has been dropped on a DropTarget which has accepted the
	 * node. This should then remove the source to prevent it from being reused.
	 */
	void onDropped(@Nonnull DropEvent context) throws Exception;

	/**
	 * Indicates that the handler is responsible for the drag and drop implementation, but does not
	 * represent the area to drag. The area to return is the area to be dragged.
	 * @return the area to be dragged in the user interface.
	 */
	@Nullable IDragArea getDragArea();

}
