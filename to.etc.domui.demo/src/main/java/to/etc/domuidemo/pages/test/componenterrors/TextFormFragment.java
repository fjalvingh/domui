package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class TextFormFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new Caption("Text component inside form"));

		TBody tb = addTable("Text2", "Text");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new Text2F4Fragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new TextF4Fragment());
	}
}
