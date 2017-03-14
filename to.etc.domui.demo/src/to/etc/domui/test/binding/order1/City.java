package to.etc.domui.test.binding.order1;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 13-3-17.
 */
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
}
