package to.etc.domuidemo.pages.test;

import to.etc.domui.dom.html.*;

public class Click2HandlerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);
		d.setHeight("200px");
		d.setBorder(1, "red", "dotted");
		d.add("Click me");
		d.setClicked2(new IClicked2<Div>() {
			@Override
			public void clicked(Div node, ClickInfo i) throws Exception {
				String s = "Click: x=" + i.getPageX() + ", y=" + i.getPageY() + ", shift=" + i.isShift() + ", ctrl=" + i.isControl() + ", alt=" + i.isAlt();
				Div d = new Div();
				d.setText(s);
				Click2HandlerPage.this.add(d);
			}
		});
		appendCreateJS("$('#" + d.getActualID() + "').disableSelection();");
	}
}
