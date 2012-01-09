package to.etc.qte;

import javax.servlet.jsp.el.*;

public class NdIf extends NdBase {
	private Expression m_expr;

	private NdList m_if;

	private NdList m_else;

	public NdIf(Expression x, NdList theif, NdList theelse) {
		m_expr = x;
		m_if = theif;
		m_else = theelse;
	}

	public NdIf(Expression x) {
		m_expr = x;
	}

	public final void setElse(NdList else1) {
		m_else = else1;
	}

	public final void setIf(NdList if1) {
		m_if = if1;
	}

	public void generate(Appendable a, VariableResolver vr) throws Exception {
		Boolean b = (Boolean) m_expr.evaluate(vr);
		if(b.booleanValue())
			m_if.generate(a, vr);
		else
			m_else.generate(a, vr);
	}
}
