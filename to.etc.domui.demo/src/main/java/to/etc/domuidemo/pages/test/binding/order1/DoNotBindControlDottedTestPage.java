package to.etc.domuidemo.pages.test.binding.order1;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.lookupinput.LookupInputBase2;
import to.etc.domui.component2.lookupinput.SameTypeModelFactory;
import to.etc.domui.derbydata.db.Customer;
import to.etc.domui.dom.html.UrlPage;


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
	 * We need a subclass with a concrete type argument so that getValue() has an
	 * actual type instead of type-erased Object. If we would not do this then
	 * the screen would fail because Object has no id property.
	 */
	public static class MyLookup extends LookupInputBase2<Customer, Customer> {
		public MyLookup() {
			super(new SameTypeModelFactory<>(), Customer.class, Customer.class);
		}
	}

	@Override public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		MyLookup li = new MyLookup();
		cp.add(li);

		li.bind("value.id").to(this, "id");
	}

	public Long getId() {
		return m_id;
	}

	public void setId(Long id) {
		m_id = id;
	}
}
