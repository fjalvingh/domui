package to.etc.server.ajax;

public class UnknownServiceMethodException extends ServiceException {
	private Class	m_serviceClass;

	private String	m_method;

	public UnknownServiceMethodException(Class clazz, String method) {
		super("The method " + method + " could not be found in the service class " + clazz.getName());
		m_serviceClass = clazz;
		m_method = method;
	}

	public UnknownServiceMethodException(Class clazz, String method, String msg) {
		super(msg);
		m_serviceClass = clazz;
		m_method = method;
	}

	public String getServiceMethod() {
		return m_method;
	}

	public Class getServiceClass() {
		return m_serviceClass;
	}
}
