package to.etc.server.ajax.renderer;

import java.io.*;
import java.util.*;
import java.util.logging.*;

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
	static private Logger	LOG			= Logger.getLogger(ObjectRenderer.class.getName());

	private RenderRegistry	m_registry;

	private IndentWriter	m_writer;

	/**
	 * This set gets filled with the "stack" of objects we are
	 * currently traversing recursively. It is used to prevent
	 * cycles in rendering, i.e. the case where a "deep" object
	 * refers to it's parent.
	 */
	private Set<Object>		m_parentSet	= new HashSet<Object>();

	public ObjectRenderer(RenderRegistry r, IndentWriter w) {
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
	final public void render(Object o) throws Exception {
		try {
			long ts = System.nanoTime();
			renderRoot(o);
			ts = System.nanoTime() - ts;
			LOG.finer("renderer completed in " + StringTool.strNanoTime(ts));
		} finally {
			m_parentSet.clear();
		}
	}

	protected void renderRoot(Object root) throws Exception {
		renderSub(root);
	}

	protected boolean isKnownObject(Object o) {
		return m_parentSet.contains(o);
	}

	protected void renderSub(Object o) throws Exception {
		//-- If the current object is on the parent stack then we have a cycle.
		if(LOG.isLoggable(Level.FINE) && o != null)
			LOG.fine("sub: type=" + o.getClass().getCanonicalName() + ": " + o);
		if(m_parentSet.contains(o)) {
			return;
			//			throw new IllegalStateException("Cycle in object tree.");
		}
		m_parentSet.add(o);
		renderPrimitive(o);
		m_parentSet.remove(o);
	}

	private void renderPrimitive(Object o) throws Exception {
		//-- Ask the renderRegistry for a renderer
		Class cl = o == null ? null : o.getClass();
		ItemRenderer ir = m_registry.makeRenderer(cl);
		ir.render(this, o);
	}

	static private final char[]	HEX	= "0123456789abcdef".toCharArray();

	public void print(byte[] bar) throws IOException {
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
	public void renderListStart(Collection l, String name) throws Exception {
	}

	@Deprecated
	public void renderListEnd(Collection l, String name) throws Exception {
	}

	public void renderArrayStart(Object l) throws Exception {
	}

	public void renderArrayEnd(Object l) throws Exception {
	}

	public void renderMapStart(Map l) throws Exception {
	}

	public void renderMapEnd(Map l) throws Exception {
	}

	public void renderMapEntry(Object key, Object value, int itemnr, int maxitemnr) throws Exception {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Object instance rendering methods.					*/
	/*--------------------------------------------------------------*/
	abstract public void renderObjectStart(Object o) throws Exception;

	abstract public void renderObjectEnd(Object o) throws Exception;

	protected void renderArrayElement(Object o, Class declaredType, int ix) throws Exception {
		renderSub(o);
	}

	protected void renderObjectMember(Object o, String name, Class declaredType) throws Exception {
		renderSub(o);
	}

	protected void renderObjectBeforeItem(int count, Object o, String name, Class declaredType) throws Exception {
	}

	protected void renderObjectAfterItem(int count, Object o, String name, Class declaredType) throws Exception {
	}
}
