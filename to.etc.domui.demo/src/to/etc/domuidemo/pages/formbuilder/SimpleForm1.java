package to.etc.domuidemo.pages.formbuilder;

import to.etc.domui.annotations.*;
import to.etc.domui.component.form.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.db.*;

public class SimpleForm1 extends UrlPage {
	private Customer m_customer;

	@UIUrlParameter(name = "id", mandatory = false)
	public Customer getCustomer() {
		if(m_customer == null) {
			m_customer = new Customer();
			m_customer.setFirstName("Frits");
			m_customer.setLastName("Jalvingh");
		}
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		m_customer = customer;
	}

	@Override
	public void createContent() throws Exception {
		TabularFormBuilder fb = new TabularFormBuilder(this);
//		fb.data(getCustomer()).addProp("firstName", "lastName", "email");
		fb.addProps("firstName", "lastName", "email");
		fb.finish();
		moveModelToControl();
	}


}
