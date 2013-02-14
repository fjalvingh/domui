package to.etc.domui.test.theme;

import org.junit.*;
import org.mozilla.javascript.*;

import to.etc.domui.server.*;
import to.etc.domui.testsupport.*;

public class TestRhino {
	private DomApplication m_da;

	@Before
	public void setUp() throws Exception {
		m_da = TUtilDomUI.getApplication();
	}

	@Test
	public void testRhino1() throws Exception {
		Context cx = Context.enter();

		try {
			Scriptable scope = cx.initStandardObjects();

			String evl = "icon = new Object(); icon['btnOkay.png\'] = 'btnOkay.svg.png?w=10&h=10';";
			cx.evaluateString(scope, evl, "<cmd>", 1, null);

			Object val = scope.get("icon", scope);
			System.out.println("Icon = " + Context.toString(val));
			if(val instanceof Scriptable) {
				Scriptable icon = (Scriptable) val;
				Object mapped = icon.get("btnOkay.png", icon);
				System.out.println("Mapped = " + mapped);
			}


		} finally {
			Context.exit();
		}


	}

}
