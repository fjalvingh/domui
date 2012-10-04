package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.pages.overview.htmleditor.*;

public class DemoFloatingWindow extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		DefaultButton db = new DefaultButton("Click me", "img/btnSmileyWink.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				FloatingWindow fw = FloatingWindow.create(d, "An example of a floating window");
				fw.setHeight("90%");
				fw.setIcon("img/btnSmileySmiley.gif");
				fw.add("Acting as a normal page; you can add all sorts of things to the page:");
				DemoHtmlEditor dhe = new DemoHtmlEditor();
				fw.add(dhe);
			}
		});
		d.add(db);
	}
}