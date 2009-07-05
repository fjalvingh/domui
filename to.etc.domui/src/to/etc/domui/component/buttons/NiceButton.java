package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;

public class NiceButton extends ATag {
	private final TextNode m_text = new TextNode("Okay");

	public NiceButton() {}

	public NiceButton(final String text) {
		m_text.setText(text);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-nbtn");
		Span sp = new Span();
		add(sp);
		sp.setCssClass("ui-nbtn-i");
		sp.add(m_text);
	}

	public String getButtonText() {
		return m_text.getText();
	}

	@Override
	public void setLiteralText(final String s) {
		m_text.setText(s);
	}
}
