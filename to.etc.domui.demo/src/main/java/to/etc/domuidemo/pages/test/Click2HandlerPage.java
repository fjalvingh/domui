package to.etc.domuidemo.pages.test;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * A click handler registered with setClicked2() gets the click's position and
 * the state of the shift, control and alt keys. Every click on the box appends
 * a line saying what arrived on the server.
 */
public class Click2HandlerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Click info"));

		Div result = new Div();
		result.setTestID("result");

		Div d = new Div();
		cp.add(d);
		d.setHeight("200px");
		d.setBorder(1, "red", "dotted");
		d.add("Click me");
		d.setTestID("clickarea");
		d.setClicked2((node, i) -> result.add(new Div("", "Click: x=" + i.getPageX() + ", y=" + i.getPageY()
			+ ", shift=" + i.isShift() + ", ctrl=" + i.isControl() + ", alt=" + i.isAlt())));

		cp.add(result);
		appendCreateJS("$('#" + d.getActualID() + "').disableSelection();");
	}
}
