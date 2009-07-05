package to.etc.domui.component.buttons;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

/**
 * A button which looks like a link.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 10, 2008
 */
public class LinkButton extends ATag {
	private String m_text;

	private String m_imageUrl;

	public LinkButton() {}

	public LinkButton(final String txt, final String image, final IClicked<LinkButton> clk) {
		setCssClass("ui-lbtn");
		setClicked(clk);
		m_text = txt;
		setImage(image);
	}

	@Override
	public void createContent() throws Exception {
		setButtonText(m_text);
	}

	public void setImage(final String url) {
		if(DomUtil.isEqual(url, m_imageUrl))
			return;
		m_imageUrl = url;
		updateStyle();
		forceRebuild();
	}

	public String getImage() {
		return m_imageUrl;
	}

	private void updateStyle() {
		setBackgroundImage(PageContext.getRequestContext().translateResourceName(m_imageUrl));
	}

	@Override
	public void setButtonText(final String txt) {
		m_text = txt;
		super.setButtonText(txt);
	}
}
