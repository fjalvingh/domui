package to.etc.server.injector;

import java.lang.reflect.*;

public class InjectorException extends Exception {
	private String	m_parameterName;

	private int		m_parameterIndex;

	private Class	m_handlerClass;

	private Method	m_handlerMethod;

	private Object	m_parameterValue;

	public Object getParameterValue() {
		return m_parameterValue;
	}

	public InjectorException setParameterValue(Object parameterValue) {
		m_parameterValue = parameterValue;
		return this;
	}

	public InjectorException(String message, Throwable cause) {
		super(message, cause);
	}

	public InjectorException(String message) {
		super(message);
	}

	public InjectorException(Throwable cause) {
		super(cause);
	}

	public Class getHandlerClass() {
		return m_handlerClass;
	}

	public InjectorException setHandlerClass(Class handlerClass) {
		m_handlerClass = handlerClass;
		return this;
	}

	public Method getHandlerMethod() {
		return m_handlerMethod;
	}

	public InjectorException setHandlerMethod(Method handlerMethod) {
		m_handlerMethod = handlerMethod;
		return this;
	}

	public int getParameterIndex() {
		return m_parameterIndex;
	}

	public InjectorException setParameterIndex(int parameterIndex) {
		m_parameterIndex = parameterIndex;
		return this;
	}

	public String getParameterName() {
		return m_parameterName;
	}

	public InjectorException setParameterName(String parameterName) {
		m_parameterName = parameterName;
		return this;
	}
}
