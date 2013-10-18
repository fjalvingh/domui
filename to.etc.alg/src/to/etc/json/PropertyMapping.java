package to.etc.json;

import java.lang.reflect.*;

public class PropertyMapping {
	final private Method m_getter;

	final private Method m_setter;

	final private String m_name;

	final private ITypeMapping m_mapper;

	public PropertyMapping(Method getter, Method setter, String name, ITypeMapping mapper) {
		m_getter = getter;
		m_setter = setter;
		m_name = name;
		m_mapper = mapper;
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

	public ITypeMapping getMapper() {
		return m_mapper;
	}
}
