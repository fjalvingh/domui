package to.etc.domui.component.misc;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class InfoPanel extends Div {
	final private String m_text;

	final private Img m_icon = new Img();

	public InfoPanel(String text) {
		this(text, "THEME/big-info.png");
	}

	public InfoPanel(String text, String icon) {
		m_text = text;
		setIcon(icon);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-ipa");
		add(m_icon);
		m_icon.setAlign(ImgAlign.LEFT);
		DomUtil.renderHtmlString(this, m_text);
	}

	public void setIcon(String rurl) {
		m_icon.setSrc(rurl);
	}

	public String getIcon() {
		return m_icon.getSrc();
	}
}
