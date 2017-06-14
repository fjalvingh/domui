package to.etc.domuidemo.pages.overview.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public class DemoSortableListTable extends UrlPage {
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
		BasicRowRenderer<Invoice> rr = new BasicRowRenderer<Invoice>(Invoice.class		//
			, "customer.firstName", SortableType.SORTABLE_ASC			//
			, "customer.lastName", SortableType.SORTABLE_ASC			//
			, "invoiceDate", SortableType.SORTABLE_ASC					//
			, "billingAddress", SortableType.SORTABLE_ASC				//
			, "billingCity", SortableType.SORTABLE_ASC					//
			, "total", SortableType.SORTABLE_ASC						//
		);
		DataTable<Invoice> dt = new DataTable<>(model, rr);
		add(dt);
		dt.setPageSize(25);
		add(new DataPager(dt));
	}


}
