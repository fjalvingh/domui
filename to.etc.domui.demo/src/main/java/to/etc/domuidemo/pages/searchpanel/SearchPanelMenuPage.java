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
		addLink(SearchPanelManual2.class, "Manual configuration: set defaults for the search values");
		addLink(SearchPanelManual3.class, "Manual configuration: use your own control");
		addLink(SearchPanelMetadata1.class, "Using metadata");
		addLink(SearchPanelMetadata2.class, "Combining metadata with manual configuration");
		addLink(SearchPanelCustomControl4.class, "Create and use a custom control");
	}
}
