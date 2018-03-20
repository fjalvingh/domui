package to.etc.domuidemo.pages.binding.xxxmodel;

import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.pages.generic.BasicListPage;
import to.etc.domui.state.UIGoto;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
public class InvoiceListPage extends BasicListPage<Invoice> {
	public InvoiceListPage() {
		super(Invoice.class);
		setAllowEmptySearch(true);
	}

	@Override protected void onNew() throws Exception {
		UIGoto.moveSub(InvoiceEditScreen.class, "invoice", "NEW");
	}

	@Override public void onSelect(@Nonnull Invoice rcord) throws Exception {
		UIGoto.moveSub(InvoiceEditScreen.class, "invoice", rcord.getId());

	}
}
