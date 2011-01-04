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

import java.io.*;

import to.etc.util.*;

/**
 * Render the tree, showing all change/dirty flags.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 3, 2008
 */
public class DumpDirtyStateRenderer extends NodeVisitorBase {
	private IndentWriter m_iw;

	private StringWriter m_sw;

	private int m_depth;

	public DumpDirtyStateRenderer() {
		m_sw = new StringWriter(8192);
		m_iw = new IndentWriter(m_sw);
	}

	static public void dump(NodeBase n) throws Exception {
		DumpDirtyStateRenderer r = new DumpDirtyStateRenderer();
		n.visit(r);
		System.out.println("---- Dirty node dump ----");
		r.m_iw.close();
		r.m_sw.close();
		System.out.println(r.m_sw.getBuffer().toString());
	}

	@Override
	public void visitNodeBase(NodeBase n) throws Exception {
		m_iw.print("[" + n.getTag() + ":" + n.getActualID() + " " + m_depth + "]");
		if(n.internalHasChangedAttributes())
			m_iw.print(" ChangedAttributes");
		if(n.isBuilt())
			m_iw.print(" built");
		if(n instanceof TextNode) {
			String txt = ((TextNode) n).getText();
			if(txt.length() > 20)
				txt = txt.substring(0, 17) + "...";
			m_iw.print(" \"" + txt + "\"");
		}
		m_iw.println();
	}

	@Override
	public void visitNodeContainer(NodeContainer n) throws Exception {
		m_iw.print("[" + n.getTag() + ":" + n.getActualID() + " " + m_depth + "]*");
		if(n.internalHasChangedAttributes())
			m_iw.print(" ChangedAttributes");
		if(n.isBuilt())
			m_iw.print(" built");
		//		if(n.internalIsTreeChanged())
		//			m_iw.print(" TreeChanged");
		if(n.childHasUpdates())
			m_iw.print(" childHasUpdates");
		if(n.mustRenderChildrenFully())
			m_iw.print(" mustRenderChFully");
		if(n.internalGetOldChildren() != null)
			m_iw.print(" oldChildren");
		m_iw.println();
		m_iw.inc();
		m_depth++;
		visitChildren(n);
		m_depth--;
		m_iw.dec();
	}
}
