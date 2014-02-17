package to.etc.domuidemo.pages;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domuidemo.sourceviewer.*;

public class MenuPage extends UrlPage {
	final private ContentPanel m_cp = new ContentPanel();

	public MenuPage(@Nonnull String pageTitle) {
		setPageTitle(pageTitle);
	}

	@Override
	protected void createFrame() throws Exception {
		add(m_cp);
		delegateTo(m_cp);
	}

	final protected void addCaption(@Nonnull String txt) {
		m_cp.add(new VerticalSpacer(10));
		m_cp.add(new CaptionedHeader(txt));
	}

	final protected void addLink(@Nonnull Class< ? extends UrlPage> clz, @Nonnull String text) {
		addLink(clz, text, false);
	}

	final protected void addLink(@Nonnull Class< ? extends UrlPage> clz, @Nonnull String text, boolean nw) {
		Div d = new Div();
		m_cp.add(d);
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
	}
}
