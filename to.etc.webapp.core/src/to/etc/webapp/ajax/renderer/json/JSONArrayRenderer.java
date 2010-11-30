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
package to.etc.webapp.ajax.renderer.json;

import java.lang.reflect.*;

import to.etc.util.*;

abstract public class JSONArrayRenderer extends JSONItemRenderer {
	private int m_perLine;

	public JSONArrayRenderer(int pl) {
		m_perLine = pl;
	}

	@Override
	final public void render(JSONRenderer r, Object val) throws Exception {
		IndentWriter w = r.getWriter();
		w.print("[");
		w.inc();
		int len = Array.getLength(val);
		int lc = 0;
		for(int i = 0; i < len; i++) {
			if(i != 0)
				w.print(",");
			render(r, val, i);
			if(lc > m_perLine) {
				lc = 0;
				w.println();
			}
		}
		w.dec();
		w.print("]");
	}

	abstract public void render(JSONRenderer r, Object val, int ix) throws Exception;
}
