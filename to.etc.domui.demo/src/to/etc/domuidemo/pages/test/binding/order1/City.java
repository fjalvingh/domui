package to.etc.domuidemo.pages.test.binding.order1;

import javax.annotation.DefaultNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
@DefaultNonNull
final public class City {
	private final Country m_country;

	private final String m_name;

	public City(Country country, String name) {
		m_country = country;
		m_name = name;
	}

	public Country getCountry() {
		return m_country;
	}

	public String getName() {
		return m_name;
	}

	@Override public String toString() {
		return m_name;
	}
}
