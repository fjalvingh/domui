package to.etc.domuidemo.pages;

import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MenuPage extends UrlPage {
	@Nullable
	private Div m_cp;

	public MenuPage(@Nonnull String pageTitle) {
		setPageTitle(pageTitle);
	}

	final protected void addCaption2(@Nonnull String txt) {
		add(new VerticalSpacer(10));
		add(new CaptionedHeader(txt));
		m_cp = new ContentPanel();
		add(m_cp);
	}


	final protected void addCaption(@Nonnull String txt) {
		Div cp = m_cp = new Div("dm-content-links");
		add(cp);
		cp.add(new HTag(2, txt));
	}

	final protected void addLink(@Nonnull Class< ? extends UrlPage> clz, @Nonnull String text) {
		addLink(clz, text, false);
	}

	final protected void addLink(@Nonnull Class< ? extends UrlPage> clz, @Nonnull String text, boolean nw) {
		Div d = new Div();
		NodeContainer cp = m_cp;
		if(null == cp) {
			cp = this;
		}
		cp.add(d);
		ALink link = new ALink(clz);
		d.add(link);
		link.setText(text);
	}
}
