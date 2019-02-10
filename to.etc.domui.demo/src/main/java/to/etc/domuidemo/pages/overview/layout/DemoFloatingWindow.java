package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.FloatingWindow;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.pages.overview.htmleditor.DemoHtmlEditor;

public class DemoFloatingWindow extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		DefaultButton db = new DefaultButton("Click me", Icon.of("img/btnSmileyWink.png"), clickednode -> {
			FloatingWindow fw = FloatingWindow.create(d, "An example of a floating window");
			fw.setHeight("90%");
			fw.setIcon("img/btnSmileySmiley.gif");
			fw.add("Acting as a normal page; you can add all sorts of things to the page:");
			DemoHtmlEditor dhe = new DemoHtmlEditor();
			fw.add(dhe);
		});
		d.add(db);
	}
}
