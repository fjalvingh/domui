package to.etc.domuidemo.pages.searchpanel;

import to.etc.domuidemo.pages.MenuPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-2-18.
 */
public class SearchPanelMenuPage extends MenuPage {
	public SearchPanelMenuPage() {
		super("LookupForm2 demo's");
	}

	@Override public void createContent() throws Exception {
		addLink(SearchPanelManual1.class, "Manual configuration");
		addLink(SearchPanelMetadata1.class, "Using metadata");
	}
}
