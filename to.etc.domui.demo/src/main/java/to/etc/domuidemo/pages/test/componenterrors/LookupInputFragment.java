package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class LookupInputFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new Caption("LookupInput components"));

		TBody tb = addTable("LookupInput2", "LookupInput1");
		//tb.getTable().setWidth("100%");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new LookupInput2Fragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new LookupInput1Fragment());
	}
}
