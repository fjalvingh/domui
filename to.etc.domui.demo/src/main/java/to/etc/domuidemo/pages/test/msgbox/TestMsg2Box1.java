package to.etc.domuidemo.pages.test.msgbox;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.dom.html.UrlPage;

/**
 * MsgBox large text auto-height but width controlled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-03-23.
 */
public class TestMsg2Box1 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Text2<Integer> text = new Text2<>(Integer.class);
		text.setMaxLength(3);
		text.setMandatory(true);
		MsgBox2.on(this)
				.input("Minutes", text)
			.onClicked(clickednode -> {
				//-- Deliberately empty
			});
	}
}
