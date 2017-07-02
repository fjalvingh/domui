package to.etc.domuidemo.pages;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Para;
import to.etc.domui.util.DomUtil;
import to.etc.domuidemo.ComponentListPage;
import to.etc.domuidemo.pages.cddb.CdCollection;
import to.etc.domuidemo.pages.test.binding.buildorder.BuildOrderPage;
import to.etc.domuidemo.pages.test.binding.conversion.BindingConversionTestForm;
import to.etc.domuidemo.pages.test.binding.order1.BindingTypeForm1;
import to.etc.domuidemo.pages.test.binding.order1.DoNotBindControlDottedTestPage;
import to.etc.domuidemo.pages.test.binding.order1.TestBindingOrder1;

public class HomePage extends MenuPage {
	public HomePage() {
		super("Component Overview - DomUI");
	}

	@Override
	public void createContent() throws Exception {
		String text = "Welcome to the DomUI demo application! This application has simple examples of many of the components. It also has some code "
			+ "from the tutorial. Use it to get "
			+ "an idea on what is possible with DomUI, and how easy it is! Click the links to go to a page, and when done use the \"breadcrumbs\" in the "
			+ "bar on top of the screen to return back to where you came from."
 			+ "<br><br>Please keep in mind: the examples here have been made as <b>simple as possible</b>. " //
			+ "Which means that the code is quite verbose sometimes. That is not how it usually is, of course, " //
			+ "it is done that way to make 'how it works' as clear as possible."
			;
		Div ip = new Div("dm-expl");
		add(ip);
		Div d = new Div();
		ip.add(d);

		Para para = new Para();
		DomUtil.renderHtmlString(para, text);
		d.add(para);
		d.add("At any time, you can press the Java icon ");
		d.add(new Img("img/java.png"));
		d.add(" to get a window showing the Java source code for the screen in question. In this window you can click the underlined class names to go to their sources too.");

		addCaption("Demo apps");
		addLink(CdCollection.class, "Tracks for sale");

		addCaption("JUnit/Selenium Test pages");
		addLink(BuildOrderPage.class, "Build order should not influence values used by bindings");
		addLink(BindingConversionTestForm.class, "Converting bindings should convert and properly send conversion errors as binding errors");
		addLink(TestBindingOrder1.class, "Bindings that depend on each other should work");
		addLink(BindingTypeForm1.class, "Binding between different types must show an error");
		addLink(DoNotBindControlDottedTestPage.class, "Binding a generic control's value as a dotted path (value.id) should throw an exception");

		addCaption("Detailed examples and wiki page");
		addLink(ComponentListPage.class, "Component overview page");
	}
}
