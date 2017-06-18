package ${package}.pages;

import to.etc.domui.dom.html.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-6-17.
 */
public class HomePage extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new HTag(1, "Hello, world"));
	}
}
