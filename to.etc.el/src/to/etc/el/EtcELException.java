package to.etc.el;

import javax.servlet.jsp.el.*;

public class EtcELException extends ELException {
	private String m_expression;

	public EtcELException() {
		super();
	}

	public EtcELException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public EtcELException(String arg0) {
		super(arg0);
	}

	public EtcELException(Throwable arg0) {
		super(arg0);
	}

	public String getExpression() {
		return m_expression;
	}

	public void setExpression(String expression) {
		m_expression = expression;
	}
}
