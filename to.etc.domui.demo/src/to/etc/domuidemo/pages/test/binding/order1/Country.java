package to.etc.domuidemo.pages.test.binding.order1;

import javax.annotation.DefaultNonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
@DefaultNonNull
public class Country {
	final private String m_name;

	final private List<City> m_cities = new ArrayList<>();

	public Country(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public List<City> getCities() {
		return m_cities;
	}

	@Override public String toString() {
		return m_name;
	}
}
