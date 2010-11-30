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

import java.io.*;
import java.util.*;

import to.etc.util.*;
import to.etc.webapp.ajax.renderer.*;

/**
 * An utility class which renders a Java object as a JSON
 * datastream.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2006
 */
public class JSONRenderer extends ObjectRenderer {
	private final boolean m_advanced;

	public JSONRenderer(final JSONRegistry r, final IndentWriter w, final boolean advanced) {
		super(r, w);
		m_advanced = advanced;
	}

	@Override
	protected void renderRoot(final Object root) throws Exception {
		super.renderRoot(root);
	}

	/**
	 * Renders the start tag for a class type to be rendered. The name of the
	 * class is the class name.
	 * @see to.etc.webapp.ajax.renderer.ObjectRenderer#renderObjectStart(java.lang.Object, java.lang.Class, java.lang.String)
	 */
	@Override
	public void renderObjectStart(final Object o) throws Exception {
		IndentWriter w = getWriter();
		//		w.forcenl();
		//		w.print(name);
		w.println("{");
		//		w.print(type.getSimpleName());
		//		w.println("*/");
		w.inc();
	}

	@Override
	public void renderObjectEnd(final Object o) throws Exception {
		IndentWriter w = getWriter();
		w.forceNewline();
		w.dec();
		w.print("}");
	}

	@Override
	protected void renderObjectBeforeItem(final int count, final Object o, final String name, final Class< ? > declaredType) throws Exception {
		if(count != 0) {
			getWriter().println(",");
		}
	}

	@Override
	public void renderArrayStart(final Object ar) throws Exception {
		getWriter().println("[");
		getWriter().inc();
	}

	@Override
	public void renderArrayEnd(final Object ar) throws Exception {
		getWriter().dec();
		getWriter().print("]");
	}

	@Override
	protected void renderArrayElement(final Object o, final Class< ? > declaredType, final int ix) throws Exception {
		if(ix > 0) {
			getWriter().print(",");
		}
		if(isKnownObject(o))
			getWriter().println("null /*was duplicate object ref*/");
		else
			renderSub(o);
	}

	@Override
	public void renderMapStart(final Map< ? , ? > l) throws Exception {
		getWriter().print("{\n");
		getWriter().inc();
	}

	@Override
	public void renderMapEnd(final Map< ? , ? > l) throws Exception {
		getWriter().dec();
		getWriter().print("}");
	}

	@Override
	public void renderMapEntry(Object key, final Object value, final int itemnr, final int maxitemnr) throws Exception {
		//-- If key is a string AND it is a reserved word rename it,
		if(key instanceof String) {
			if(isReservedWord((String) key))
				key = "_" + key;
		}
		renderSub(key);
		getWriter().print(": ");
		renderSub(value);
		if(itemnr + 1 < maxitemnr)
			getWriter().print(",");
	}

	static private boolean isReservedWord(final String k) {
		return k.equals("function");
	}

	public void printInt(final int i) throws IOException {
		getWriter().print(Integer.toString(i));
	}

	/**
	 * Generate a class member in JSON syntax, which is name: value
	 *
	 * @see to.etc.webapp.ajax.renderer.ObjectRenderer#renderObjectMember(java.lang.Object, java.lang.String, java.lang.Class)
	 */
	@Override
	protected void renderObjectMember(final Object o, String name, final Class< ? > declaredType) throws Exception {
		IndentWriter w = getWriter();
		if(isReservedWord(name))
			name = "_" + name;
		w.print(name);
		w.print(": ");
		renderSub(o);
	}

	public void renderDate(final Date dt) throws Exception {
		if(!m_advanced) {
			getWriter().print(Long.toString(dt.getTime()));
			return;
		}

		//		getWriter().print("JSONUtils.newDate(");
		//		getWriter().print(Long.toString(dt.getTime()));
		//		getWriter().print(")");
		getWriter().print("new Date(");
		getWriter().print(Long.toString(dt.getTime()));
		getWriter().print(")");

	}
}
