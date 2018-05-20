package to.etc.domuidemo.pages.binding.xxxmodel;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.pages.generic.BasicListPage;
import to.etc.domui.state.UIGoto;

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

	@Override public void onSelect(@NonNull Invoice rcord) throws Exception {
		UIGoto.moveSub(InvoiceEditScreen.class, "invoice", rcord.getId());

	}
}
