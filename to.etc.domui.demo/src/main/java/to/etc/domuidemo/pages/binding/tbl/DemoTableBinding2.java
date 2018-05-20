package to.etc.domuidemo.pages.binding.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.tbl.DataPager;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.IRowControlFactory;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.IRenderInto;
import to.etc.domuidemo.pages.WikiExplanationPage;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

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
			@NonNull @Override public IControl<?> createControl(@NonNull Employee rowInstance) throws Exception {
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

//	private void clickedOne(@NonNull final Employee a) {
//		//-- Change the artist's name field.
//		String name = a.getName();
//		name = name.substring(1) + name.substring(0, 1);
//		a.setName(name);
//	}

}
