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

	public LinkButton(final String txt, final String image) {
		setCssClass("ui-lbtn");
		m_text = txt;
		setImage(image);
	}

	public LinkButton(final String txt) {
		setCssClass("ui-lnkb");
		m_text = txt;
	}

	public LinkButton(final String txt, final IClicked<LinkButton> clk) {
		setCssClass("ui-lnkb");
		setClicked(clk);
		m_text = txt;
	}

	@Override
	public void createContent() throws Exception {
		setText(m_text);
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
		if(m_imageUrl == null) {
			setBackgroundImage(null);
			setCssClass("ui-lnkb");
		} else {
			setBackgroundImage(PageContext.getRequestContext().translateResourceName(m_imageUrl));
			setCssClass("ui-lnkb ui-lbtn");
		}
	}

	@Override
	public void setText(final String txt) {
		m_text = txt;
		super.setText(txt);
	}
}
