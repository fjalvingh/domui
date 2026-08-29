package to.etc.domuidemo.pages.tutorial.first;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "building your first page", step 2: a page is a tree of tags, and
 * every tag is added to the tag that must contain it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HelloTreePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("A tree of tags");

		Div box = new Div("dm-tut");
		add(box);

		box.add(new HTag(1, "Hello, world!"));

		Para para = new Para();
		box.add(para);
		para.add("This paragraph sits inside a div, and this ");
		para.add(new Span("dm-tut-hi", "span"));
		para.add(" sits inside the paragraph. What you add something to decides where it ends up.");
	}
}
