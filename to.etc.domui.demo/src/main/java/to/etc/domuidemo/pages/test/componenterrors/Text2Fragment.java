package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class Text2Fragment extends Div {
	@Override public void createContent() throws Exception {
		add(new Caption("Text2 component"));

		TBody tb = addTable("With form builder", "Without form builder");
		//tb.getTable().setWidth("100%");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new Text2F4Fragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new Text2RawFragment());
	}
}
