package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdQuestion extends NdBase {
	private NdBase m_cond;

	private NdBase m_ifpart;

	private NdBase m_elsepart;

	/**
	 * @param a
	 * @param b
	 */
	public NdQuestion(NdBase a, NdBase b) {
		m_cond = a;
		m_ifpart = b;
	}

	public void setElse(NdBase e) {
		m_elsepart = e;
	}

	/**
	 * See jsp 2.0, JSP2.3.5.2
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object cond = m_cond.evaluate(vr);
		boolean va = RuntimeConversions.convertToBool(cond);
		if(va)
			return m_ifpart.evaluate(vr);
		else
			return m_elsepart.evaluate(vr);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		m_cond.getExpression(a);
		a.append('?');
		m_ifpart.getExpression(a);
		a.append(':');
		m_elsepart.getExpression(a);
	}
}
