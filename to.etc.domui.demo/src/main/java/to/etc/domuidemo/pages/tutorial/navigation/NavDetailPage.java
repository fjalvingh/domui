package to.etc.domuidemo.pages.tutorial.navigation;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IShelvedEntry;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;

import java.util.List;

/**
 * Tutorial, "page navigation", step 2: the shelved page stack, which is what the
 * breadcrumb at the top of this page is made of, and what each move does to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NavDetailPage extends UrlPage {
	private int m_level = 1;

	@Override
	public String getPageTitle() {
		return "Detail " + m_level;
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Detail page, level " + m_level));

		//-- Show the shelve stack this page is on: the breadcrumb above is made of it.
		List<IShelvedEntry> stack = UIContext.getRequestContext().getWindowSession().getShelvedPageStack();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < stack.size(); i++) {
			IShelvedEntry se = stack.get(i);
			sb.append(i).append(": ").append(se.getName()).append("   ").append(se.getURL()).append("\n");
		}

		Div shelve = new Div("dm-tut-q");
		cp.add(shelve);
		shelve.add(sb.toString());

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Deeper (moveSub)", a -> UIGoto.moveSub(NavDetailPage.class, "level", m_level + 1));
		bb.addButton("Sideways (replace)", a -> UIGoto.replace(NavDetailPage.class, "level", m_level + 1));
		bb.addButton("Start over here (moveNew)", a -> UIGoto.moveNew(NavDetailPage.class, "level", 0));
		bb.addButton("The page I came from (moveSub)", a -> UIGoto.moveSub(NavStatePage.class));
	}

	public int getLevel() {
		return m_level;
	}

	@UIUrlParameter(name = "level", mandatory = false)
	public void setLevel(int level) {
		m_level = level;
	}
}
