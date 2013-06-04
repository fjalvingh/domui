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

public interface INodeContentRenderer<T> {
	/**
	 * Render the content for a node. You should add whatever is needed to render the value of "object" to the "node" parameter, as either text
	 * or other DomUI nodes.
	 *
	 * jal 20130211 object MUST be @Nullable, because lots of code depends on needing to render an empty value structure (for instance LookupInputBase).
	 *
	 * @param component
	 * @param node
	 * @param object				The nullable item we're rendering.
	 * @param parameters
	 * @throws Exception
	 */
	void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nonnull T object, @Nullable Object parameters) throws Exception;
}
