package to.etc.el;

public class ELPropertyException extends EtcELException {
	private Class m_beanClass;

	private String m_property;

	public ELPropertyException(Class cl, String property) {
		super("No property '" + property + "' found on bean class=" + cl.getName());
		m_beanClass = cl;
		m_property = property;
	}

	public Class getBeanClass() {
		return m_beanClass;
	}

	public String getProperty() {
		return m_property;
	}
}
