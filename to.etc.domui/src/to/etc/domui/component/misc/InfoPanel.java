package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

public class InfoPanel extends Div {
	private String		m_text;

	public InfoPanel(String text) {
		m_text = text;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-ipa");
		Img img = new Img("THEME/big-info.png");
		add(img);
		img.setAlign(ImgAlign.LEFT);
		add(m_text);
	}
}
