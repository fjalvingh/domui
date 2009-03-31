package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

public class Explanation extends Div {
	private LiteralXhtml	m_text = new LiteralXhtml();

	public Explanation(String txt) {
		setCssClass("ui-expl");
		setText(txt);
	}
	@Override
	public void createContent() throws Exception {
		Img	i = new Img("THEME/big-info.png");
		i.setAlign(ImgAlign.LEFT);
		add(i);
		add(m_text);
	}

	@Override
	public void setText(String txt) {
		m_text.setXml(txt);
	}
}
