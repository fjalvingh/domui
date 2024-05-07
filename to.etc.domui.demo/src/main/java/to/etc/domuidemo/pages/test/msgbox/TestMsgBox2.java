package to.etc.domuidemo.pages.test.msgbox;

import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.pages.Lorem;

/**
 * MsgBox large text auto-size without scrollbar..
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-03-23.
 */
public class TestMsgBox2 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		MsgBox.error(this, Lorem.getSentences(850));
	}
}
