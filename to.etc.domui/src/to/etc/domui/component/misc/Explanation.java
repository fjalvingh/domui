package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;

public class Explanation extends Div {
	private final XmlTextNode m_text = new XmlTextNode();

	public Explanation(final String txt) {
		setCssClass("ui-expl");
		setText(txt);
	}

	@Override
	public void createContent() throws Exception {
		Img i = new Img("THEME/big-info.png");
		i.setAlign(ImgAlign.LEFT);
		add(i);
		add(m_text);
	}

	@Override
	public void setText(final String txt) {
		m_text.setText(txt);
	}
}
