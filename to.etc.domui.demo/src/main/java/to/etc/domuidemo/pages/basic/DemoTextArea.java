package to.etc.domuidemo.pages.basic;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.dom.html.HTag;

/**
 * Test page for showing TextArea.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 2, 2010
 */
public class DemoTextArea extends UrlPage {
	private TextArea m_area;

	private ContentPanel m_cp;

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = m_cp = new ContentPanel();
		add(cp);

		cp.add(new HTag(1, "Text area test."));
		m_area = new TextArea(80, 5);
		cp.add(m_area);

		DefaultButton b = new DefaultButton("Save", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				handleClick();
			}
		});
		cp.add(b);
	}

	void handleClick() {
		String text = m_area.getValue();
		System.out.println("----- Value ----\n" + text + "\n----- End value ----");

		//-- Add a new div containing that value
		Div d = new Div();
		d.setCssClass("ui-pre");
		d.add(text);
		m_cp.add(d);
	}
}
