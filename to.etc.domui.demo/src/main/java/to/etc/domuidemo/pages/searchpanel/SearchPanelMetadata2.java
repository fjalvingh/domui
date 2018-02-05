package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.derbydata.db.Invoice;

/**
 * Combining metadata with user customization.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelMetadata2 extends AbstractSearchPage<Invoice> {
	public SearchPanelMetadata2() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class);
		cp.add(lf);

		lf.add().property("billingAddress").control();

		lf.addDefault();

		lf.setClicked(a -> search(lf.getCriteria()));
	}
}
