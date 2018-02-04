package to.etc.domuidemo.pages.lookupform2;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.lookupform2.LookupForm2;
import to.etc.domui.derbydata.db.Invoice;

/**
 * A lookup form using properties to define what fields to lookup on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class Lookup2Manual extends AbstractSearchPage<Invoice> {
	public Lookup2Manual() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		LookupForm2<Invoice> lf = new LookupForm2<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf));

		lf.add().property("customer").control();			// Start with lookup by customer
		lf.add().property("total").control();				// Allow searching for a total
		lf.add().property("invoiceDate").control();			// And the date.
	}
}
