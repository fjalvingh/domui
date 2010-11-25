package to.etc.util;

/**
 * Thrown when a runtime conversion fails.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class RuntimeConversionException extends RuntimeException {
	private String	m_where;

	private String	m_message;

	public RuntimeConversionException() {
	}

	public RuntimeConversionException(Object in, String to) {
		m_message = "Cannot convert object type " + (in == null ? "(null)" : in.getClass().getName()) + " to " + to + "( value=" + in + ")";
	}

	public RuntimeConversionException(String message) {
		m_message = message;
	}

	public RuntimeConversionException(String message, Throwable cause) {
		super(message, cause);
		m_message = message;
	}

	public RuntimeConversionException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		if(m_message == null) {
			if(m_where == null)
				return "Unknown conversion error";
			return "Unknown conversion error: " + m_where;
		}
		if(m_where == null)
			return m_message;
		return m_message + " " + m_where;
	}

	public void setWhere(String s) {
		m_where = s;
	}
}
