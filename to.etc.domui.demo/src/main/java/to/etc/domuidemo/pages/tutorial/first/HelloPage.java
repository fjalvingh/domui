package to.etc.domuidemo.pages.tutorial.first;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "building your first page", step 1: the smallest page there is - a
 * div with a bit of text in it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HelloPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("My first page");

		Div box = new Div("dm-tut");
		add(box);
		box.add("Hello, world!");
	}
}
