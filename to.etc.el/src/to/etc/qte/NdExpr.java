package to.etc.qte;

import javax.servlet.jsp.el.*;

public class NdExpr extends NdBase {
	private Expression m_expr;

	public NdExpr(Expression x) {
		m_expr = x;
	}

	public void generate(Appendable a, VariableResolver vr) throws Exception {
		String o = (String) m_expr.evaluate(vr);
		if(o == null)
			return;
		a.append(o);
	}
}
