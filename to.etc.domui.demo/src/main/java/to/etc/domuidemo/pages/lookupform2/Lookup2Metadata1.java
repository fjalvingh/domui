package to.etc.domuidemo.pages.lookupform2;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.lookupform2.LookupForm2;
import to.etc.domui.derbydata.db.Invoice;

/**
 * The simplest LookupForm one can get using metadata.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-1-18.
 */
public class Lookup2Metadata1 extends AbstractSearchPage<Invoice> {
	public Lookup2Metadata1() {
		super(Invoice.class);
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		LookupForm2<Invoice> lf = new LookupForm2<>(Invoice.class);
		cp.add(lf);
		lf.setClicked(a -> search(lf));
	}
}
