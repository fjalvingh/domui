package to.etc.domui.fd;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * DomUI form designer.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 30, 2013
 */
public class FormDesigner extends UrlPage {
	private PaintPanel m_paint;

	@Override
	public void createContent() throws Exception {
		getPage().addHeaderContributor(HeaderContributor.loadStylesheet("fd/css/formbuilder.css"), 100);
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("fd/js/formbuilder.js"), 100);

		Div top = new Div();
		add(top);
		top.setCssClass("fb-top");

		top.add("Form Designer");

		Div left = new Div();
		add(left);
		left.setCssClass("fb-left");

		setCssClass("fb-main");
		m_paint = new PaintPanel();
		left.add(m_paint);

		Div right = new Div();
		add(right);
		right.setCssClass("fb-right");


	}

}
