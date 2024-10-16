package to.etc.domui.test.theme;

import org.junit.*;

import to.etc.domui.server.*;
import to.etc.domui.testsupport.*;
import to.etc.domui.themes.*;
import to.etc.util.*;

public class TestThemeExpander {
	private DomApplication m_da;

	@Before
	public void setUp() throws Exception {
		m_da = TUtilDomUI.getApplication();
	}

	// Cannot run without an application - fixed in 4.0-lookandfeel branch
	//	@Test
	//	public void testThemeExpander1() throws Exception {
	//		FragmentedThemeFactory cfc = new FragmentedThemeFactory();
	//		CssPropertySet ps = cfc.getProperties("themes/domui", "style.props.js", null);
	//
	//		System.out.println("Loaded " + ps.getMap().size() + " properties");
	//	}

	@Test
	public void testThemeExpander2() throws Exception {
		FragmentedThemeFactory cfc = new FragmentedThemeFactory();

		long ts = System.nanoTime();
		cfc.loadTheme(m_da);
		ts = System.nanoTime() - ts;
		System.out.println("Loaded in " + StringTool.strNanoTime(ts));
	}

	@Test
	public void testThemeExpander4() throws Exception {
		FragmentedThemeFactory cfc = new FragmentedThemeFactory();
		long ts = System.nanoTime();
		cfc.loadStyleInfo(m_da);
		ts = System.nanoTime() - ts;
		System.out.println("Loaded in " + StringTool.strNanoTime(ts));
	}
}
