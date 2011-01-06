package to.etc.domui.themes;

import org.mozilla.javascript.*;

public class JavascriptExecutor {
	private final JavascriptExecutorFactory m_factory;

	private Scriptable m_scope;

	public JavascriptExecutor(JavascriptExecutorFactory javascriptExecutorFactory) {
		m_factory = javascriptExecutorFactory;
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
			//			jcx.evaluateString()


			m_scope = jcx.newObject(rootScope);
			m_scope.setPrototype(rootScope);
			m_scope.setParentScope(null);
		} finally {
			Context.exit();
		}
	}


}
