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
package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * DEPRECATED: Try to use XmlTextNode, instead. This is a component which allows it's content to be literal XHTML.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 9, 2008
 */
@Deprecated
public class LiteralXhtml extends NodeBase {
	private String m_xml;

	public LiteralXhtml() {
		super("div");
		setCssClass("ui-lxh");
	}

	@Override
	public void visit(final INodeVisitor v) throws Exception {
		v.visitLiteralXhtml(this);
	}

	public String getXml() {
		return m_xml;
	}

	public void setXml(final String xml) {
		if(DomUtil.isEqual(xml, m_xml))
			return;
		StringBuilder sb = new StringBuilder(xml.length());
		try {
			StringTool.entitiesToUnicode(sb, xml, true);
		} catch(Exception x) {
			throw new WrappedException(x);
		}
		m_xml = sb.toString();
		changed();
	}
}
