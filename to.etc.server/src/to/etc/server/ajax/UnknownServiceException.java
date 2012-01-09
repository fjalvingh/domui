package to.etc.server.ajax;

import to.etc.server.servlet.*;

/**
 * Gets thrown when a given service is unknown.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2006
 */
public class UnknownServiceException extends ServiceException {
	private String	m_serviceType;

	private String	m_serviceName;

	private String	m_serviceClass;

	private String	m_serviceMethod;

	public UnknownServiceException(RequestContext ctx, String svctype, String serviceName, String message, Throwable cause) {
		super(ctx, message, cause);
		m_serviceType = svctype;
		m_serviceName = serviceName;
	}

	public UnknownServiceException(RequestContext ctx, String svctype, String serviceName, String message) {
		super(ctx, message);
		m_serviceType = svctype;
		m_serviceName = serviceName;
	}

	public UnknownServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownServiceException(String message, String cn, String mn) {
		super(message);
		m_serviceClass = cn;
		m_serviceMethod = mn;
	}

	public String getServiceClass() {
		return m_serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		m_serviceClass = serviceClass;
	}

	public String getServiceMethod() {
		return m_serviceMethod;
	}

	public void setServiceMethod(String serviceMethod) {
		m_serviceMethod = serviceMethod;
	}

	public String getServiceName() {
		return m_serviceName;
	}

	public void setServiceName(String serviceName) {
		m_serviceName = serviceName;
	}

	public String getServiceType() {
		return m_serviceType;
	}

	public void setServiceType(String serviceType) {
		m_serviceType = serviceType;
	}
}
