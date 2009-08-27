package to.etc.util;

import java.lang.reflect.*;

/**
 * Information on properties on a class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 9, 2007
 */
final public class PropertyInfo {
	private String	m_name;

	private Method	m_getter;

	private Method	m_setter;

	public PropertyInfo(String name, Method getter, Method setter) {
		m_name = name;
		m_getter = getter;
		m_setter = setter;
	}

	public String getName() {
		return m_name;
	}

	public Method getGetter() {
		return m_getter;
	}

	public Method getSetter() {
		return m_setter;
	}
}
