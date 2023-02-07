package to.etc.domuidemo.pages.overview.tbl;

import to.etc.domui.component.misc.Explanation;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SortableListModel;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.derbydata.db.Invoice_;
import to.etc.domui.dom.css.ClearType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

import java.util.List;

final public class DemoSortableListTable extends UrlPage {
	private List<Invoice> m_list;

	@Override
	public void createContent() throws Exception {
		Explanation explanation = new Explanation("This shows how you can use a SortableListModel to manipulate any kind of Java List<T> into a data table.");
		add(explanation);
		Div d = new Div();
		add(d);
		d.setClear(ClearType.BOTH);

		//-- 1. Prepare a list to use as the basis of the model. Although this comes from the database any list will do of course.
		m_list = getSharedContext().query(QCriteria.create(Invoice.class));

		//-- 2. Create the model.
		SortableListModel<Invoice> model = new SortableListModel<Invoice>(Invoice.class, m_list);		// Oh, the joys of repeating yourself 8-(


		//-- Now the presentation.
		RowRenderer<Invoice> rr = new RowRenderer<>(Invoice.class);
		rr.column(Invoice_.customer().firstName()).ascending();
		rr.column(Invoice_.customer().lastName()).ascending();
		rr.column(Invoice_.invoiceDate()).ascending();
		rr.column(Invoice_.billingAddress()).ascending();
		rr.column(Invoice_.billingCity()).ascending();
		rr.column(Invoice_.total()).ascending();
		DataTable<Invoice> dt = new DataTable<>(model, rr);
		add(dt);
		dt.setPageSize(25);
		add(new DataPager(dt));
	}
}
