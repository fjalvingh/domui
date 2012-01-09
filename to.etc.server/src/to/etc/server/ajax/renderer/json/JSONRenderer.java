package to.etc.server.ajax.renderer.json;

import java.io.*;
import java.util.*;

import to.etc.server.ajax.renderer.*;
import to.etc.util.*;

/**
 * An utility class which renders a Java object as a JSON
 * datastream.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2006
 */
public class JSONRenderer extends ObjectRenderer {
	private boolean	m_advanced;

	public JSONRenderer(JSONRegistry r, IndentWriter w, boolean advanced) {
		super(r, w);
		m_advanced = advanced;
	}

	@Override
	protected void renderRoot(Object root) throws Exception {
		super.renderRoot(root);
	}

	/**
	 * Renders the start tag for a class type to be rendered. The name of the
	 * class is the class name.
	 * @see to.etc.server.ajax.renderer.ObjectRenderer#renderObjectStart(java.lang.Object, java.lang.Class, java.lang.String)
	 */
	@Override
	public void renderObjectStart(Object o) throws Exception {
		IndentWriter w = getWriter();
		//		w.forcenl();
		//		w.print(name);
		w.println("{");
		//		w.print(type.getSimpleName());
		//		w.println("*/");
		w.inc();
	}

	@Override
	public void renderObjectEnd(Object o) throws Exception {
		IndentWriter w = getWriter();
		w.forceNewline();
		w.dec();
		w.print("}");
	}

	@Override
	protected void renderObjectBeforeItem(int count, Object o, String name, Class declaredType) throws Exception {
		if(count != 0) {
			getWriter().println(",");
		}
	}

	@Override
	public void renderArrayStart(Object ar) throws Exception {
		getWriter().println("[");
		getWriter().inc();
	}

	@Override
	public void renderArrayEnd(Object ar) throws Exception {
		getWriter().dec();
		getWriter().print("]");
	}

	@Override
	protected void renderArrayElement(Object o, Class declaredType, int ix) throws Exception {
		if(ix > 0) {
			getWriter().print(",");
		}
		if(isKnownObject(o))
			getWriter().println("null /*was duplicate object ref*/");
		else
			renderSub(o);
	}

	@Override
	public void renderMapStart(Map l) throws Exception {
		getWriter().print("{\n");
		getWriter().inc();
	}

	@Override
	public void renderMapEnd(Map l) throws Exception {
		getWriter().dec();
		getWriter().print("}");
	}

	@Override
	public void renderMapEntry(Object key, Object value, int itemnr, int maxitemnr) throws Exception {
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

	static private boolean isReservedWord(String k) {
		return k.equals("function");
	}

	public void printInt(int i) throws IOException {
		getWriter().print(Integer.toString(i));
	}

	/**
	 * Generate a class member in JSON syntax, which is name: value
	 *
	 * @see to.etc.server.ajax.renderer.ObjectRenderer#renderObjectMember(java.lang.Object, java.lang.String, java.lang.Class)
	 */
	@Override
	protected void renderObjectMember(Object o, String name, Class declaredType) throws Exception {
		IndentWriter w = getWriter();
		if(isReservedWord(name))
			name = "_" + name;
		w.print(name);
		w.print(": ");
		renderSub(o);
	}

	public void renderDate(Date dt) throws Exception {
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
