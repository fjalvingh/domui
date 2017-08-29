package to.etc.domuidemo.pages;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domuidemo.sourceviewer.*;

import javax.annotation.*;

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

		ALink link2 = new ALink(SourcePage.class, new PageParameters("name", clz.getName().replace('.', '/') + ".java"));
		d.add("\u00a0");
		d.add(link2);
		Img si = new Img("img/java.png");
		link2.add(si);
		link2.setTitle("View sourcefile");
		if(nw)
			d.add(new Img("img/aniNew.gif"));
		link2.setNewWindowParameters(WindowParameters.createFixed(1024, 768, "source"));
	}
}
