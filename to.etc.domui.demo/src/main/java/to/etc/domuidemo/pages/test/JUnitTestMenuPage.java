package to.etc.domuidemo.pages.test;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.test.binding.buildorder.BuildOrderPage;
import to.etc.domuidemo.pages.test.binding.conversion.BindingConversionTestForm;
import to.etc.domuidemo.pages.test.binding.order1.BindingTypeForm1;
import to.etc.domuidemo.pages.test.binding.order1.DoNotBindControlDottedTestPage;
import to.etc.domuidemo.pages.test.binding.order1.TestBindingOrder1;
import to.etc.domuidemo.pages.overview.allcomponents.AllComponents1Page;
import to.etc.domuidemo.pages.test.componenterrors.Form4LayoutTestPage;
import to.etc.domuidemo.pages.test.componenterrors.HtmlEditorTestPage;
import to.etc.domuidemo.pages.test.componenterrors.LookupForm1TestPage;
import to.etc.domuidemo.pages.test.componenterrors.LookupForm2TestPage;
import to.etc.domuidemo.pages.test.componenterrors.LookupInput2TestPage;
import to.etc.domuidemo.pages.test.componenterrors.LookupInputTestPage;
import to.etc.domuidemo.pages.test.componenterrors.Text2LayoutTestPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-8-17.
 */
public class JUnitTestMenuPage extends MenuPage {
	public JUnitTestMenuPage() {
		super("Test Pages");
	}

	@Override public void createContent() throws Exception {
		Div panel = new Div("dm-expl-panel");
		add(panel);
		panel.add(new HTag(1, "What are these pages about?"));
		Div ip = new Div("dm-expl");
		panel.add(ip);
		ip.add("These pages are used in JUnit and Selenium tests to check whether DomUI works as it should");

		addCaption("Data Binding test pages");
		addLink(BuildOrderPage.class, "Build order should not influence values used by bindings");
		addLink(BindingConversionTestForm.class, "Converting bindings should convert and properly send conversion errors as binding errors");
		addLink(TestBindingOrder1.class, "Bindings that depend on each other should work");
		addLink(BindingTypeForm1.class, "Binding between different types must show an error");
		addLink(DoNotBindControlDottedTestPage.class, "Binding a generic control's value as a dotted path (value.id) should throw an exception");

		addCaption("forms and components");
		addLink(AllComponents1Page.class, "All components look overview page");
		addLink(Form4LayoutTestPage.class, "Form4 vertical form builder layout");
		addLink(Text2LayoutTestPage.class, "Text2 layout");
		addLink(LookupForm1TestPage.class, "LookupForm tests: clearInput with LookupInput mandatory control");
		addLink(LookupForm2TestPage.class, "LookupForm tests: same, checks that clearInput is implemented");

		addCaption("Component test pages");
		addLink(HtmlEditorTestPage.class, "Test htmleditor");
		addLink(LookupInput2TestPage.class, "Test LookupInput2");
		addLink(RowRendererFactoryTest.class, "A factory that is not editable, should be editable by default");

		addCaption("Deprecated components' test");
		addLink(LookupInputTestPage.class, "Test LookupInput (deprecated)");
	}
}
