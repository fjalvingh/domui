package to.etc.domuidemo.pages.binding.tbl;

import to.etc.domui.component.input.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.domuidemo.pages.*;
import to.etc.webapp.query.*;

import javax.annotation.*;

public class DemoTableBinding2 extends WikiExplanationPage {
	@Override
	public void createContent() throws Exception {
		QDataContext dc = getSharedContext();
		QCriteria<Employee> q = QCriteria.create(Employee.class);

		SimpleSearchModel<Employee> sm = new SimpleSearchModel<Employee>(this, q);
		RowRenderer<Employee> rr = new RowRenderer<Employee>(Employee.class);
		rr.column("firstName").label("First Name").ascending().editable();
		rr.column("lastName").label("Last Name").ascending().sortdefault().editable();

		final IRenderInto<Employee> contentRenderer = new IRenderInto<Employee>() {
			@Override
			public void render(NodeContainer node, Employee object) throws Exception {
				node.add(object.getFirstName() + " " + object.getLastName());
			}
		};

		rr.column(Employee.class, "reportsTo").label("Manager").editable().factory(new IRowControlFactory<Employee>() {
			@Nonnull @Override public IControl<?> createControl(@Nonnull Employee rowInstance) throws Exception {
				LookupInput<Employee> li = new LookupInput<Employee>(Employee.class);
				li.setValueRenderer(contentRenderer);
				return li;
			}
		});

		rr.column("email").label("email").ascending().editable();
		rr.column("phone").label("phone");
		rr.column("title").label("Title");
//		rr.column("address").label("Address");
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
