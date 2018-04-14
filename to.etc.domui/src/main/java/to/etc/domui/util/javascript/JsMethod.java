package to.etc.domui.util.javascript;

import org.eclipse.jdt.annotation.NonNull;

public class JsMethod {
	@NonNull
	private final JavascriptStmt m_stmt;

	private JsMethod m_currentMethod;

	public JsMethod(@NonNull JavascriptStmt stmt) {
		m_stmt = stmt;
	}

	public void flush() {
		m_stmt.sb().append(")");
	}

	@NonNull
	public JsMethod arg(@NonNull Object what) throws Exception {
		comma();
		m_stmt.object(what);
		return this;
	}

	private void comma() {
		char lc = m_stmt.lastChar();
		if(lc != '(')
			m_stmt.sb().append(',');
	}

	@NonNull
	public JsMethod var(@NonNull String variableName) {
		comma();
		m_stmt.sb().append(variableName);
		return this;
	}

//	@NonNull
//	private JsMethod endfn() {
//		JsMethod jm = m_currentMethod;
//		if(null != jm) {
//			m_currentMethod = null;
//			jm.flush();
//		}
//		return this;
//	}
//
//	@NonNull
//	public JsMethod fn(@NonNull String name) {
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

	@NonNull
	public JavascriptStmt end() {
		m_stmt.endmethod();
		return m_stmt;
	}
}
