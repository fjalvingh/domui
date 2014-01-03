package to.etc.domuidemo.pages;

import to.etc.domui.dom.html.*;

public class WikiFragment extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-wiki");

		UrlPage page = getPage().getBody();
		String classname = page.getClass().getName();
		classname = classname.substring(classname.lastIndexOf('.') + 1);
		String url = "http://www.domui.org/wiki/bin/view/Documentation/" + classname + "?xpage=print";

		IFrame frame = new IFrame();
		add(frame);
		frame.setFrameHeight("100%");
		frame.setFrameWidth("100%");
		frame.setSrc(url);
	}
}
