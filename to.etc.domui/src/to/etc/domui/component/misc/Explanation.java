package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

public class Explanation extends Div {
	private final LiteralXhtml	m_text = new LiteralXhtml();

	public Explanation(final String txt) {
		setCssClass("ui-expl");
		setButtonText(txt);
	}
	@Override
	public void createContent() throws Exception {
		Img	i = new Img("THEME/big-info.png");
		i.setAlign(ImgAlign.LEFT);
		add(i);
		add(m_text);
	}

	@Override
	public void setButtonText(final String txt) {
		m_text.setXml(txt);
	}

	@Override
	public void	setText(final BundleRef b, final String key) {
		m_text.setXml(b.getString(key));
	}
}
