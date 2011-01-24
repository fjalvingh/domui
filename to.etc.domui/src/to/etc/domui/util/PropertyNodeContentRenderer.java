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
import to.etc.webapp.nls.*;

/**
 * Renders the content for a node by looking up a property value of the specified class and rendering that one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
public class PropertyNodeContentRenderer<T> implements INodeContentRenderer<T> {
	@Nonnull
	final private PropertyValueConverter<T> m_converter;

	public PropertyNodeContentRenderer(@Nonnull PropertyValueConverter<T> converter) {
		m_converter = converter;
	}

	public PropertyNodeContentRenderer(String... properties) {
		m_converter = new PropertyValueConverter<T>(properties);
	}

	public void renderNodeContent(NodeBase component, NodeContainer node, T object, Object parameters) throws Exception {
		String val = m_converter.convertObjectToString(NlsContext.getLocale(), object);
		node.add(val);
	}
}
