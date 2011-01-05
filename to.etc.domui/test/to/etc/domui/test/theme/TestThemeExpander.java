package to.etc.domui.test.theme;

import org.junit.*;

import to.etc.domui.server.*;
import to.etc.domui.test.util.*;
import to.etc.domui.themes.*;

public class TestThemeExpander {
	private DomApplication m_da;

	@Before
	public void setUp() throws Exception {
		m_da = DomUITestUtil.getApplication();
	}


	@Test
	public void testThemeExpander1() throws Exception {
		CssFragmentCollector cfc = new CssFragmentCollector(m_da, "$themes/domui");
		cfc.loadStyleProperties();


	}


}
