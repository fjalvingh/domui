package to.etc.domuidemo.pages.overview.allcomponents;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class TextFragment extends Div {
	@Override public void createContent() throws Exception {
		add(new Caption("Text component"));

		TBody tb = addTable("With form builder", "Without form builder");
		TD td = tb.addRowAndCell();
		td.setWidth("600px");
		td.add(new Text1F4Fragment());
		td = tb.addCell();
		td.setWidth("600px");
		td.add(new TextRawFragment());
	}
}
