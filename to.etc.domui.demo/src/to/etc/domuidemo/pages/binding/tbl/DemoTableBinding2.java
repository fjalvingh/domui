package to.etc.domuidemo.pages.binding.tbl;

import to.etc.domui.component.tbl.*;
import to.etc.domuidemo.db.*;
import to.etc.domuidemo.pages.*;
import to.etc.webapp.query.*;

public class DemoTableBinding2 extends WikiExplanationPage {
	@Override
	public void createContent() throws Exception {
		QDataContext dc = getSharedContext();
		QCriteria<Employee> q = QCriteria.create(Employee.class);

		SimpleSearchModel<Employee> sm = new SimpleSearchModel<Employee>(this, q);
		RowRenderer<Employee> rr = new RowRenderer<Employee>(Employee.class);
		rr.column("firstName").label("First Name").ascending().editable();
		rr.column("lastName").label("Last Name").ascending().sortdefault().editable();
		rr.column("email").label("email").ascending().editable();
		rr.column("phone").label("phone");
		rr.column("title").label("Title");
		rr.column("address").label("Address");
		rr.column("postalCode").label("Postal code");

		DataTable<Employee> dt = new DataTable<Employee>(sm, rr);
		add(dt);
		dt.setPageSize(25);
		add(new DataPager(dt));

//		rr.setRowClicked(new ICellClicked<Artist>() {
//			@Override
//			public void cellClicked(NodeBase tr, Artist rowval) throws Exception {
//				clickedOne(rowval);
//			}
//		});
	}

//	private void clickedOne(@Nonnull final Employee a) {
//		//-- Change the artist's name field.
//		String name = a.getName();
//		name = name.substring(1) + name.substring(0, 1);
//		a.setName(name);
//	}

}
