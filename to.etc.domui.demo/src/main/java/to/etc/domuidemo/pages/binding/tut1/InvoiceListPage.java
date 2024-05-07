package to.etc.domuidemo.pages.binding.tut1;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domuidemo.pages.special.BasicListPage;
import to.etc.domui.state.UIGoto;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
@NonNullByDefault
final public class InvoiceListPage extends BasicListPage<Invoice> {
	public InvoiceListPage() {
		super(Invoice.class);
		setAllowEmptySearch(true);
	}

	@Override protected void onNew() throws Exception {
		UIGoto.moveSub(EditInvoicePageB1.class, "invoice", "NEW");
	}

	@Override public void onSelect(@NonNull Invoice rcord) throws Exception {
		UIGoto.moveSub(EditInvoicePageB1.class, "invoice", rcord.getId());
	}
}
