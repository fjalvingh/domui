package to.etc.iocular.def;

import to.etc.util.*;

/**
 * A configuration-time definition for setting a specific property to some specific value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 18, 2009
 */
public class ComponentPropertyDef {
	private final ComponentBuilder	m_builder;
	private final String			m_propertyName;
	private String					m_sourceName;
	private Class<?>				m_sourceClass;
	private boolean					m_required;
	private PropertyInfo			m_info;

	ComponentPropertyDef(final ComponentBuilder builder, final String propertyName) {
		m_builder = builder;
		m_propertyName = propertyName;
	}

	public String getSourceName() {
		return m_sourceName;
	}
	public void setSourceName(final String sourceName) {
		m_sourceName = sourceName;
	}

	public Class< ? > getSourceClass() {
		return m_sourceClass;
	}
	public void setSourceClass(final Class< ? > sourceClass) {
		m_sourceClass = sourceClass;
	}

	public ComponentBuilder getBuilder() {
		return m_builder;
	}
	public String getPropertyName() {
		return m_propertyName;
	}

	/**
	 * When T this property MUST be settable. It is set for all explicitly defined properties and for
	 * the properties added when the property mode is 'allProperties'. It is unset for automatically
	 * added properties in 'knownProperties' mode.
	 * @return
	 */
	public boolean isRequired() {
		return m_required;
	}
	public void setRequired(final boolean required) {
		m_required = required;
	}

	PropertyInfo getInfo() {
		return m_info;
	}
	void setInfo(final PropertyInfo info) {
		m_info = info;
	}
}
