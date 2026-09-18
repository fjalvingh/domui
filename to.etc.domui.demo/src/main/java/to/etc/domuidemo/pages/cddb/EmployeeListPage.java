package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Employee_;
import to.etc.domui.state.UIGoto;

/**
 * The shop's staff.
 */
public class EmployeeListPage extends AbstractCdShopListPage<Employee> {
	public EmployeeListPage() {
		super(Employee.class, "Staff");
	}

	@Override
	protected void configureColumns(@NonNull RowRenderer<Employee> rr) throws Exception {
		rr.column(Employee_.lastName()).ascending().sortdefault();
		rr.column(Employee_.firstName()).ascending();
		rr.column(Employee_.title());
		rr.column(Employee_.city());
		rr.column(Employee_.dateHired());
	}

	@Override
	protected void onRowSelected(@NonNull Employee instance) throws Exception {
		UIGoto.moveSub(EmployeeDetailPage.class, "id", instance.getId());
	}
}
