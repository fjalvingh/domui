package to.etc.domui.test.binding.order1;

import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
public class TestBindingOrder1 extends UrlPage {
	private List<Country> m_countryList = new ArrayList<>();

	public TestBindingOrder1() {
		
	}

	private void register(String country, String... cities) {
		Country c = new Country(country);
		for(String s: cities) {
			City cty = new City(c, s);
			c.getCities().add(cty);
		}
		m_countryList.add(c);
	}

	@Override public void createContent() throws Exception {




	}
}
