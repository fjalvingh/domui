package to.etc.domuidemo.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;

public class MenuPage extends UrlPage {
	@Nullable
	private Div m_cp;

	public MenuPage(@NonNull String pageTitle) {
		setPageTitle(pageTitle);
	}

	final protected void addCaption2(@NonNull String txt) {
		add(new VerticalSpacer(10));
		add(new CaptionedHeader(txt));
		m_cp = new ContentPanel();
		add(m_cp);
	}


	final protected void addCaption(@NonNull String txt) {
		Div cp = m_cp = new Div("dm-content-links");
		add(cp);
		cp.add(new HTag(2, txt));
	}

	final protected void addLink(@NonNull Class< ? extends UrlPage> clz, @NonNull String text) {
		addLink(clz, text, false);
	}

	final protected void addLink(@NonNull Class< ? extends UrlPage> clz, @NonNull String text, boolean nw) {
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
