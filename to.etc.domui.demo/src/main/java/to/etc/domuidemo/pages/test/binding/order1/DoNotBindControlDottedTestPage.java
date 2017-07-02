package to.etc.domuidemo.pages.test.binding.order1;

import to.etc.domui.component.input.LookupInput;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;


/**
 * Test for bug #2: cannot bind a control property with a dotted path. Opening
 * this form must throw a {@link to.etc.domui.component.binding.BindingFailureException}
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-3-17.
 */
public class DoNotBindControlDottedTestPage extends UrlPage {
	private Long m_id;

	/**
	 * We need to extends LookupInput so that getValue() has an actual
	 * type instead of type-erased Object. If we would not do this then
	 * the screen would fail because Object has no id property.
	 */
	public class MyLookup extends LookupInput<Customer> {
		public MyLookup() {
			super(QCriteria.create(Customer.class));
		}
	}

	@Override public void createContent() throws Exception {
		MyLookup li = new MyLookup();
		add(li);

		li.bind("value.id").to(this, "id");
	}

	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}
}
