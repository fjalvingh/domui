package to.etc.iocular.def;

/**
 * Specification of a method parameter by the builder.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2009
 */
public class MethodParameterSpec {
	/** When set this is defined as "get the parameter by looking up this type in the container" */
	private Class<?>			m_sourceType;

	/** When set this is defined as "lookup the defined component with this name in the container" */
	private String				m_sourceName;

	/** When defined this parameter refers to the component that is being defined. */
	private boolean				m_self;

	/** For numbered parameters this defines the formal parameter that must be set by this parameter */
	private int					m_parameterNumber;

	public Class< ? > getSourceType() {
		return m_sourceType;
	}
	public void setSourceType(final Class< ? > sourceType) {
		m_sourceType = sourceType;
	}
	public String getSourceName() {
		return m_sourceName;
	}
	public void setSourceName(final String sourceName) {
		m_sourceName = sourceName;
	}
	public boolean isSelf() {
		return m_self;
	}
	public void setSelf(final boolean self) {
		m_self = self;
	}
	public int getParameterNumber() {
		return m_parameterNumber;
	}
	public void setParameterNumber(final int parameterNumber) {
		m_parameterNumber = parameterNumber;
	}
}
