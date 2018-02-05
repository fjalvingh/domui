package to.etc.domuidemo.pages.searchpanel;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.derbydata.db.Invoice;

/**
 * The simplest LookupForm one can get using metadata.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class SearchPanelMetadata1 extends AbstractSearchPage<Invoice> {
	public SearchPanelMetadata1() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		SearchPanel<Invoice> lf = new SearchPanel<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf.getCriteria()));
	}
}
