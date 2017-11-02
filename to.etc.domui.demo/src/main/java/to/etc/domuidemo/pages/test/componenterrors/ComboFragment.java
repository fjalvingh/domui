package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-11-17.
 */
public class ComboFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new Caption("Combo components inside form"));

		TBody tb = addTable("ComboXxxx2", "ComboXxxx");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new Combo2FFragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new ComboF4Fragment());
	}
}
