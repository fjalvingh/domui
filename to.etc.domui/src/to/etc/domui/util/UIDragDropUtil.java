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

import to.etc.domui.dom.html.*;

public final class UIDragDropUtil {
	private UIDragDropUtil() {}

	/**
	 * Expose all draggable thingies on a node.
	 * @param base
	 * @param dh
	 */
	static public void exposeDraggable(NodeBase base, IDragHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drgbl");
			//			base.setOnMouseDownJS(null);	jal 20110104 Should only be cleared for draggables..
		} else {
			base.addCssClass("ui-drgbl");
			base.setOnMouseDownJS("WebUI.dragMouseDown(this, event)");
			base.setSpecialAttribute("uitype", dh.getTypeName(base));
			if(dh.getDragArea() != null)
				base.setSpecialAttribute("dragarea", dh.getDragArea().getActualID());
		}
	}

	static public void exposeDroppable(NodeBase base, IDropHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drpbl");
		} else {
			base.addCssClass("ui-drpbl");
			StringBuilder sb = new StringBuilder();
			if(dh.getAcceptableTypes() == null)
				throw new IllegalStateException("The IDropHandler for node " + base + " (" + dh + ") returns a null list of acceptable types");
			for(String s : dh.getAcceptableTypes()) {
				if(sb.length() != 0)
					sb.append(',');
				sb.append(s);
			}
			base.setSpecialAttribute("uitypes", sb.toString());
		}
	}
}
