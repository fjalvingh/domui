package to.etc.domui.themes;

import org.mozilla.javascript.*;

/**
 * Execute Javascript code, using Rhino. The JDK embedded scripting engine
 * sucks like a Nilfisk: it is a severely abused version of Rhino that is
 * inaccessible by code outside the scripting engine. Consequently it can
 * only be used to do pathetic simple things.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 6, 2011
 */
public class JavascriptExecutorFactory {
	/** This is the root Javascript scope, containing things like "Function", "Object" and other fun and games. */
	private ScriptableObject	m_rootScope;

	public synchronized void initialize() {
		if(m_rootScope != null)
			return;

		Context jcx = Context.enter();
		try {
			m_rootScope = jcx.initStandardObjects(null, true); // SEAL all standard library object in scope but allow other additions.

			//			// Force the LiveConnect stuff to be loaded.
			//			String loadMe = "RegExp; getClass; java; Packages; JavaAdapter;";
			//			jcx.evaluateString(m_rootScope, loadMe, "lazyLoad", 0, null);
			//			m_rootScope.sealObject();
		} finally {
			Context.exit();
		}
	}

	public JavascriptExecutor createExecutor() {
		initialize();

		JavascriptExecutor jx = new JavascriptExecutor(this);
		jx.initialize(m_rootScope);
		return jx;


	}
}
