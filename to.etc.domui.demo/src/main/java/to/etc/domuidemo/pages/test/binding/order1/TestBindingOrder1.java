package to.etc.domuidemo.pages.test.binding.order1;

import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
public class TestBindingOrder1 extends UrlPage {
	private List<Country> m_countryList = new ArrayList<>();

	private Country	m_country;

	private City m_city;

	public TestBindingOrder1() {
		register("Netherlands", "Amsterdam", "The Hague", "Maastricht", "Lelystad");
		register("USA", "New York", "Boston", "Washington");
		register("Iceland", "Reykjavík", "Vatnajökulsþjóðgarður", "Bláskógabyggð");
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
		ComboLookup2<Country> cl = new ComboLookup2<>(m_countryList);

		List<City> cities = new ArrayList<>();
		m_countryList.forEach(country -> cities.addAll(country.getCities()));
		ComboLookup2<City> cil = new ComboLookup2<>(cities);
		cil.immediate();
		cl.immediate();

		FormBuilder fb = new FormBuilder(this);
		fb.property(this, "country").label("Country").control(cl);
		fb.property(this, "city").label("City").control(cil);
	}

	public Country getCountry() {
		return m_country;
	}

	public void setCountry(Country country) {
		m_country = country;
		City city = getCity();
		if(country == null) {
			m_city = null;
		} else if(city == null || city.getCountry() != country) {
			m_city = country.getCities().get(0);
		}
	}

	public City getCity() {
		return m_city;
	}

	public void setCity(City city) {
		m_city = city;
		if(city != null) {
			m_country = city.getCountry();
		}
	}
}
