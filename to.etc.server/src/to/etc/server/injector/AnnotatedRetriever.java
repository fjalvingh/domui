package to.etc.server.injector;

import to.etc.server.ajax.*;

abstract public class AnnotatedRetriever implements Retriever {
	private String	m_name;

	private boolean	m_hasDefault;

	private String	m_defval;

	private Class	m_type;

	abstract public void releaseObject(Object o);

	abstract protected Object retrieveValuePrimitive(Object source) throws Exception;

	public AnnotatedRetriever(Class type, String name, AjaxParam ap) {
		this(type, name, ap == null ? Injector.NOVALUE : ap.dflt());
	}

	public AnnotatedRetriever(Class type, String name, String defdef) {
		m_type = type;
		m_name = name;
		m_hasDefault = !defdef.equals(Injector.NOVALUE);
		m_defval = defdef;
	}

	public Class getType() {
		return m_type;
	}

	final public Object retrieveValue(Object source) throws Exception {
		Object val = retrieveValuePrimitive(source);
		if(val != NO_VALUE)
			return val;
		return getDefaultValue();
	}

	/**
	 * Returns the default value for a parameter, or throws an exception
	 * if there is no default value.
	 * @return
	 * @throws Exception
	 */
	protected Object getDefaultValue() throws Exception {
		if(!m_hasDefault)
			throw new ParameterException("Missing mandatory value for AJAX parameter '" + m_name + "' (" + m_type + ")").setParameterName(m_name);
		return m_defval;
	}
}
