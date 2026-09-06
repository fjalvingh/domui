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
package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

import to.etc.function.IExecute;

public interface IClicked2<T extends NodeBase> extends IClickBase<T> {
	/**
	 * This gets called when the node is clicked. The parameter is the node that the click
	 * handler was attached to. Since the node itself is passed you can easily reuse a click
	 * handler instance for several same-type nodes.
	 * @param node
	 * @param clinfo
	 * @throws Exception
	 */
	void clicked(@NonNull T node, @NonNull ClickInfo clinfo) throws Exception;

	/**
	 * Wrap a parameterless action as a click handler, discarding both the clicked node and
	 * the {@link ClickInfo}.
	 */
	@NonNull
	static <T extends NodeBase> IClicked2<T> wrap(@NonNull IExecute action) {
		return (node, clinfo) -> action.execute();
	}
}
