package to.etc.domuidemo.pages.cddb;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.derbydata.db.Customer_;
import to.etc.domui.state.UIGoto;

/**
 * Find a customer of the shop.
 */
public class CustomerListPage extends AbstractCdShopListPage<Customer> {
	public CustomerListPage() {
		super(Customer.class, "Customers");
	}

	@Override
	protected void configureColumns(@NonNull RowRenderer<Customer> rr) throws Exception {
		rr.column(Customer_.lastName()).ascending().sortdefault();
		rr.column(Customer_.firstName()).ascending();
		rr.column(Customer_.city()).ascending();
		rr.column(Customer_.country()).ascending();
		rr.column(Customer_.email());
	}

	@Override
	protected void onRowSelected(@NonNull Customer instance) throws Exception {
		UIGoto.moveSub(CustomerDetailPage.class, "id", instance.getId());
	}
}
