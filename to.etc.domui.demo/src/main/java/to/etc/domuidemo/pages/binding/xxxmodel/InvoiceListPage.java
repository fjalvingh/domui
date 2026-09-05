package to.etc.domuidemo.pages.binding.xxxmodel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.webapp.query.QCriteria;

/**
 * Search for an invoice and open it in the binding tutorial's edit screen.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-3-18.
 */
@NonNullByDefault
public class InvoiceListPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Invoices"));

		SearchPanel<Invoice> sp = new SearchPanel<>(Invoice.class);
		cp.add(sp);

		Div results = new Div();
		cp.add(results);

		sp.setClicked(a -> showResult(results, sp.getCriteria()));
		sp.setOnNew(a -> UIGoto.moveSub(InvoiceEditScreen.class, "invoice", "NEW"));
	}

	private void showResult(NodeContainer target, @Nullable QCriteria<Invoice> criteria) throws Exception {
		if(null == criteria)						// Nothing entered, or an input error
			return;
		RowRenderer<Invoice> rr = new RowRenderer<>(Invoice.class);
		rr.setRowClicked(invoice -> UIGoto.moveSub(InvoiceEditScreen.class, "invoice", invoice.getId()));

		DataTable<Invoice> table = new DataTable<>(new SimpleSearchModel<>(this, criteria), rr);
		table.setPageSize(15);

		target.removeAllChildren();
		target.add(table);
		target.add(new DataPager(table));
	}
}
