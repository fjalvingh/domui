package to.etc.domuidemo.pages.test.msgbox;

import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBox2.Type;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

/**
 * MsgBox2 should have a scrollbar if the content does not fit (large div)..
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-03-23.
 */
public class TestMsg2Box6 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div large = new Div();
		large.setBackgroundColor("red");
		large.setWidth("100px");
		large.setHeight("700px");

		MsgBox2.on(this)
			.type(Type.INFO)
			.size(800, 500)
			.content(large)
			.button(MsgBoxButton.CONTINUE)
			.onClicked(clickednode -> {
				//-- Deliberately empty
			});
	}
}
