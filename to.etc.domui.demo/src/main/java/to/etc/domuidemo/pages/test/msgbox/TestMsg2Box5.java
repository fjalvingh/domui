package to.etc.domuidemo.pages.test.msgbox;

import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBox2.Type;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.pages.Lorem;

/**
 * MsgBox2 should have a scrollbar if the content does not fit (text)..
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-03-23.
 */
public class TestMsg2Box5 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		MsgBox2.on(this)
			.type(Type.INFO)
			.size(800, 500)
			.text(Lorem.getSentences(10500))
			.button(MsgBoxButton.CONTINUE)
			.onClicked(clickednode -> {
				//-- Deliberately empty
			});
	}
}
