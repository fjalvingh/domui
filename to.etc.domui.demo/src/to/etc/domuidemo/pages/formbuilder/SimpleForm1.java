package to.etc.domuidemo.pages.formbuilder;

import to.etc.domui.annotations.*;
import to.etc.domui.component.builder.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.db.*;

public class SimpleForm1 extends UrlPage {
	private Customer m_customer;

	@UIUrlParameter(name = "id", mandatory = false)
	public Customer getCustomer() {
		if(m_customer == null) {
			m_customer = new Customer();
		}
		return m_customer;
	}

	public void setCustomer(Customer customer) {
		m_customer = customer;
	}

	@Override
	public void createContent() throws Exception {
		add(new AppPageTitleBar("Test form #1", false));

		FormBuilder fb = new FormBuilder(this);
//		fb.data(getCustomer()).addProp("firstName", "lastName", "email");
		fb.data(getCustomer()).addProps("firstName", "lastName", "email");
		fb.finish();
		fb.getBindings().moveModelToControl();
	}


}
