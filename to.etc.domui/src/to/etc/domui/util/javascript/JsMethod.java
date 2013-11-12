package to.etc.domui.util.javascript;

import javax.annotation.*;

public class JsMethod {
	@Nonnull
	private final JavascriptStmt m_stmt;

	private JsMethod m_currentMethod;

	public JsMethod(@Nonnull JavascriptStmt stmt) {
		m_stmt = stmt;
	}

	public void flush() {
		m_stmt.sb().append(")");
	}

	@Nonnull
	public JsMethod arg(@Nonnull Object what) throws Exception {
		comma();
		m_stmt.object(what);
		return this;
	}

	private void comma() {
		char lc = m_stmt.lastChar();
		if(lc != '(')
			m_stmt.sb().append(',');
	}

	@Nonnull
	public JsMethod var(@Nonnull String variableName) {
		comma();
		m_stmt.sb().append(variableName);
		return this;
	}

//	@Nonnull
//	private JsMethod endfn() {
//		JsMethod jm = m_currentMethod;
//		if(null != jm) {
//			m_currentMethod = null;
//			jm.flush();
//		}
//		return this;
//	}
//
//	@Nonnull
//	public JsMethod fn(@Nonnull String name) {
//		endfn();
//		StringBuilder sb = m_stmt.sb();
//		char lc = m_stmt.lastChar();
//		if(lc != 0 && lc != ';' && lc != '.' && lc != '(') {
//			sb.append('.');
//		}
//		sb.append(name);
//		sb.append('(');
//		return m_currentMethod = new JsMethod(this);
//	}

	@Nonnull
	public JavascriptStmt end() {
		m_stmt.endmethod();
		return m_stmt;
	}
}
