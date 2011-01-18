package to.etc.domui.test.js;

import org.junit.*;

import to.etc.domui.themes.*;

public class TestJavascript {
	private JavascriptExecutorFactory m_jsx;

	@Before
	public void init() {
		m_jsx = new JavascriptExecutorFactory();
	}

	@Test
	public void testExec1() throws Exception {
		JavascriptExecutor jx = m_jsx.createExecutor();

		Object o = jx.eval("new Object();");
		System.out.println("Obj=" + o);
	}

}
