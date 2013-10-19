package to.etc.domui.util.javascript;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.json.*;
import to.etc.util.*;

public class Stmt {
	@Nonnull
	final private StringBuilder m_sb;

	private boolean m_instmt;

	private JsMethod m_currentMethod;

	Stmt(@Nonnull StringBuilder worksb) {
		m_sb = worksb;
	}

	@Nonnull
	StringBuilder sb() {
		return m_sb;
	}

	@Nonnull
	public Stmt next() {
		if(m_sb.length() == 0)
			return this;
		char last = m_sb.charAt(m_sb.length() - 1);
		if(last != ';')
			m_sb.append(";");
		m_instmt = false;
		return this;
	}

	/**
	 * Add a JQuery selector.
	 * @param node
	 * @return
	 */
	@Nonnull
	public Stmt select(@Nonnull NodeBase node) {
		return select(node.getActualID());
	}

	/**
	 * Add a JQuery selector.
	 * @param id
	 * @return
	 */
	@Nonnull
	public Stmt select(@Nonnull String id) {
		sb().append("$('#").append(id).append("')");
		m_instmt = true;
		return this;
	}

	char lastChar() {
		int length = m_sb.length();
		if(length == 0)
			return 0;
		return m_sb.charAt(length - 1);
	}

	/**
	 * Just append a verbatim string.
	 * @param string
	 * @return
	 */
	@Nonnull
	public Stmt append(@Nonnull String string) {
		sb().append(string);
		return this;
	}

	/**
	 * Render any kind of Java object as a Javascript thingy.
	 * @param object
	 * @return
	 */
	@Nonnull
	public Stmt object(@Nonnull Object object) throws Exception {
		if(object == null) {
			sb().append("null");
		} else if(object instanceof String) {
			StringTool.strToJavascriptString(sb(), (String) object, false);
		} else if(object instanceof Number) {
			sb().append(object.toString());
		} else if(object instanceof java.util.Date) {
			sb().append(((java.util.Date) object).getTime());
		} else if(object instanceof Boolean) {
			sb().append(((Boolean) object).toString());
		} else {
			//-- Use JSON marshaller
			sb().append(JSON.render(object));
		}
		return this;
	}

	@Nonnull
	public Stmt endmethod() {
		JsMethod jm = m_currentMethod;
		if(null != jm) {
			m_currentMethod = null;
			jm.flush();
		}
		return this;
	}

	@Nonnull
	public JsMethod method(@Nonnull String name) {
		endmethod();
		char lc = lastChar();
		if(lc != 0 && lc != ';' && lc != '.' && lc != '(') {
			sb().append('.');
		}
		sb().append(name);
		sb().append('(');
		return m_currentMethod = new JsMethod(this);
	}

	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		Stmt st = new Stmt(sb);

		st.select("_IZ").method("options").arg("hello").arg(Integer.valueOf(1)).arg(Boolean.TRUE).end().next();

		System.out.println(sb.toString());

		sb.setLength(0);

		st.select("_IZ").method("options").arg("hello").arg(Integer.valueOf(1)).arg(Boolean.TRUE).end().next();


	}
}
