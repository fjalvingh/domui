package to.etc.domui.themes;

import java.io.*;

import org.mozilla.javascript.*;

public class JavascriptExecutor {
	//	private final JavascriptExecutorFactory m_factory;

	private Scriptable m_scope;

	public JavascriptExecutor(JavascriptExecutorFactory javascriptExecutorFactory) {
		//		m_factory = javascriptExecutorFactory;
	}

	/**
	 * Create the local scope for this executor, inheriting the root scope containing
	 * Object, Function and other stuff.
	 * @param rootScope
	 */
	public void initialize(ScriptableObject rootScope) {
		Context jcx = Context.enter();
		try {
			m_scope = jcx.newObject(rootScope);
			m_scope.setPrototype(rootScope);
			m_scope.setParentScope(null);
		} finally {
			Context.exit();
		}
	}

	public Object eval(String js) throws Exception {
		Context jcx = Context.enter();
		try {
			return jcx.evaluateString(m_scope, js, "inline", 1, null);
		} finally {
			Context.exit();
		}
	}

	public Object eval(Reader r, String jsname) throws Exception {
		Context jcx = Context.enter();
		try {
			return jcx.evaluateReader(m_scope, r, jsname, 1, null);
		} finally {
			Context.exit();
		}
	}

	public Scriptable getScope() {
		return m_scope;
	}

	public Object toObject(Object o) {
		return Context.toObject(o, m_scope);
	}
}
