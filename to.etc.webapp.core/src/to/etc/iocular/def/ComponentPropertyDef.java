package to.etc.iocular.def;

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
}
