package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;

public class NiceButton extends ATag {
	private TextNode		m_text = new TextNode("Okay");

	public NiceButton() {
	}
	public NiceButton(String text) {
		m_text.setText(text);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-nbtn");
		Span	sp	= new Span();
		add(sp);
		sp.setCssClass("ui-nbtn-i");
		sp.add(m_text);
	}

	public String	getText() {
		return m_text.getText();
	}
	@Override
	public void		setText(String s) {
		m_text.setText(s);
	}
}
