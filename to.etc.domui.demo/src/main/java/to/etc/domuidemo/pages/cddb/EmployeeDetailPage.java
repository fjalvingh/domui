package to.etc.domuidemo.pages.cddb;

import to.etc.domui.annotations.UIUrlParameter;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.TabPanel;
import to.etc.domui.component.masterchild.ChildFragment;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.SimpleSearchModel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Employee_;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;
import to.etc.webapp.query.QCriteria;

/**
 * A member of staff. The two things you want to see next to someone's details are
 * who reports to them and which customers they look after, so both are here as
 * detail parts: the first bound to the employee's own collection, the second
 * queried.
 */
public class EmployeeDetailPage extends UrlPage {
	private Employee m_employee;

	@Override
	public String getPageTitle() {
		return m_employee.getId() == null ? "New employee" : m_employee.getFirstName() + " " + m_employee.getLastName();
	}

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Employee"));

		FormBuilder fb = new FormBuilder(cp);
		fb.property(m_employee, Employee_.firstName()).mandatory().control();
		fb.property(m_employee, Employee_.lastName()).mandatory().control();
		fb.property(m_employee, Employee_.title()).control();
		fb.property(m_employee, Employee_.birthDate()).control();
		fb.property(m_employee, Employee_.dateHired()).control();
		fb.property(m_employee, Employee_.email()).mandatory().control();
		fb.property(m_employee, Employee_.phone()).control();

		fb.property(m_employee, Employee_.address()).control();
		fb.property(m_employee, Employee_.postalCode()).control();
		fb.property(m_employee, Employee_.city()).control();
		fb.property(m_employee, Employee_.country()).control();

		//-- Who this employee reports to; anyone but themselves.
		fb.property(m_employee, Employee_.reportsTo())
			.control(new ComboLookup2<>(QCriteria.create(Employee.class).ascending("lastName")));

		cp.add(new VerticalSpacer(10));

		TabPanel tp = new TabPanel();
		cp.add(tp);
		tp.add(createReportsTab(), "Reporting to this employee");
		tp.add(createCustomerTab(), "Customers supported");

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Save", Theme.BTN_SAVE, a -> save());
	}

	private Div createReportsTab() throws Exception {
		Div d = new Div();
		ChildFragment<Employee, Employee> reports = new ChildFragment<>(m_employee, Employee_.reportsFrom());
		d.add(reports);
		reports.column(Employee_.lastName()).label("Last name").ascending().sortdefault();
		reports.column(Employee_.firstName()).label("First name");
		reports.column(Employee_.title()).label("Function");
		reports.onClick(e -> UIGoto.moveSub(EmployeeDetailPage.class, "id", e.getId()));
		return d;
	}

	private Div createCustomerTab() throws Exception {
		Div d = new Div();
		QCriteria<Customer> q = QCriteria.create(Customer.class)
			.eq(Customer_.supportRepresentative(), m_employee)
			.ascending(Customer_.lastName());

		RowRenderer<Customer> rr = new RowRenderer<>(Customer.class);
		rr.column(Customer_.lastName()).label("Last name").ascending().sortdefault();
		rr.column(Customer_.firstName()).label("First name");
		rr.column(Customer_.city()).label("City");
		rr.column(Customer_.country()).label("Country");
		rr.setRowClicked(c -> UIGoto.moveSub(CustomerDetailPage.class, "id", c.getId()));

		DataTable<Customer> dt = new DataTable<>(new SimpleSearchModel<>(this, q), rr);
		dt.setPageSize(10);
		d.add(dt);
		return d;
	}

	private void save() throws Exception {
		if(bindErrors())
			return;
		try {
			getSharedContext().save(m_employee);
			getSharedContext().commit();
			UIGoto.back();
		} catch(Exception x) {
			ExceptionDialog.create(this, "Save failed", x);
		}
	}

	@UIUrlParameter(name = "id")
	public Employee getEmployee() {
		return m_employee;
	}

	public void setEmployee(Employee employee) {
		m_employee = employee;
	}
}
