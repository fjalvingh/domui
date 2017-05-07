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
package to.etc.webapp.ajax.renderer;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import to.etc.util.*;

/**
 * A basic object renderer which traverses an object tree and calls handler methods
 * to render the object's components. This is the base class for the JSON and XML
 * renderers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2006
 */
abstract public class ObjectRenderer {
	static private Logger LOG = LoggerFactory.getLogger(ObjectRenderer.class);

	private final RenderRegistry m_registry;

	private final IndentWriter m_writer;

	/**
	 * This set gets filled with the "stack" of objects we are
	 * currently traversing recursively. It is used to prevent
	 * cycles in rendering, i.e. the case where a "deep" object
	 * refers to it's parent.
	 */
	private final Set<Object> m_parentSet = new HashSet<Object>();

	public ObjectRenderer(final RenderRegistry r, final IndentWriter w) {
		m_registry = r;
		m_writer = w;
	}

	final public IndentWriter getWriter() {
		return m_writer;
	}

	final public RenderRegistry getRegistry() {
		return m_registry;
	}

	/**
	 * This is the main entrypoint for the renderer. It causes the object passed to be
	 * rendered.
	 *
	 * @param o
	 * @throws Exception
	 */
	final public void render(final Object o) throws Exception {
		try {
			long ts = System.nanoTime();
			renderRoot(o);
			ts = System.nanoTime() - ts;
			LOG.debug("renderer completed in " + StringTool.strNanoTime(ts));
		} finally {
			m_parentSet.clear();
		}
	}

	protected void renderRoot(final Object root) throws Exception {
		renderSub(root);
	}

	protected boolean isKnownObject(final Object o) {
		return m_parentSet.contains(o);
	}

	protected void renderSub(final Object o) throws Exception {
		//-- If the current object is on the parent stack then we have a cycle.
		if(LOG.isDebugEnabled() && o != null)
			LOG.debug("sub: type=" + o.getClass().getCanonicalName() + ": " + o);
		if(m_parentSet.contains(o)) {
			return;
			//			throw new IllegalStateException("Cycle in object tree.");
		}
		m_parentSet.add(o);
		renderPrimitive(o);
		m_parentSet.remove(o);
	}

	private void renderPrimitive(final Object o) throws Exception {
		//-- Ask the renderRegistry for a renderer
		Class< ? > cl = o == null ? null : o.getClass();
		ItemRenderer ir = m_registry.makeRenderer(cl);
		ir.render(this, o);
	}

	static private final char[] HEX = "0123456789abcdef".toCharArray();

	public void print(final byte[] bar) throws IOException {
		int len = bar.length;
		if(len == 0)
			return;
		char[] buf = new char[128];
		int ix = 0;
		int off = 0;

		while(ix < len) {
			if(off >= 128) {
				getWriter().write(buf);
				off = 0;
			}
			byte v = bar[ix++];
			int v1 = (v & 0xf);
			buf[off++] = HEX[v1];
			v1 = ((v >> 4) & 0xf);
			buf[off++] = HEX[v1];
		}
		if(off > 0)
			getWriter().write(buf, 0, off);
	}


	@Deprecated
	public void renderListStart(final Collection< ? > l, final String name) throws Exception {}

	@Deprecated
	public void renderListEnd(final Collection< ? > l, final String name) throws Exception {}

	public void renderArrayStart(final Object l) throws Exception {}

	public void renderArrayEnd(final Object l) throws Exception {}

	public void renderMapStart(final Map< ? , ? > l) throws Exception {}

	public void renderMapEnd(final Map< ? , ? > l) throws Exception {}

	public void renderMapEntry(final Object key, final Object value, final int itemnr, final int maxitemnr) throws Exception {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object instance rendering methods.					*/
	/*--------------------------------------------------------------*/
	abstract public void renderObjectStart(Object o) throws Exception;

	abstract public void renderObjectEnd(Object o) throws Exception;

	protected void renderArrayElement(final Object o, final Class< ? > declaredType, final int ix) throws Exception {
		renderSub(o);
	}

	protected void renderObjectMember(final Object o, final String name, final Class< ? > declaredType) throws Exception {
		renderSub(o);
	}

	protected void renderObjectBeforeItem(final int count, final Object o, final String name, final Class< ? > declaredType) throws Exception {}

	protected void renderObjectAfterItem(final int count, final Object o, final String name, final Class< ? > declaredType) throws Exception {}
}
