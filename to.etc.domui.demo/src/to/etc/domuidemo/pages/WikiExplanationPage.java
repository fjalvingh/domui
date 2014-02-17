package to.etc.domuidemo.pages;

import to.etc.domui.dom.html.*;
import to.etc.domuidemo.components.*;

/**
 * Base page for examples that also shows a wiki article.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2014
 */
public class WikiExplanationPage extends UrlPage {
	private Div m_content;

	private WikiFragment m_wiki;

	@Override
	protected void createFrame() throws Exception {
		add(new SourceBreadCrumb());
		Div d = m_content = new Div();
		add(d);
		d.setCssClass("ui-demo-content");
		add(m_wiki = new WikiFragment());
		delegateTo(d);
	}
}
