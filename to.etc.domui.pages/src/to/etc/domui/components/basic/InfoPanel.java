package to.etc.domui.components.basic;

import to.etc.domui.dom.html.*;

public class InfoPanel extends Div {
	private String m_text;

	public InfoPanel(String text) {
		m_text = text;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("bb-info");
		Img img = new Img("img/info.png");
		add(img);
		img.setAlign(ImgAlign.LEFT);
		addLiteral(m_text);
	}
}
