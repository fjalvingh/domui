package to.etc.el;

import javax.servlet.jsp.el.*;

import to.etc.el.node.*;

public class ElExpressionImpl extends Expression {
	/** The compiled form of the expression. */
	private NdBase m_node;

	private String m_expr;

	ElExpressionImpl(NdBase root, String expr) {
		m_node = root;
		m_expr = expr;
	}

	@Override
	public Object evaluate(VariableResolver arg0) throws ELException {
		try {
			return m_node.evaluate(arg0);
		} catch(EtcELException x) {
			x.setExpression(m_expr);
			throw x;
		} catch(ELException x) {
			throw x;
		} catch(RuntimeException x) {
			x.printStackTrace();
			throw new ELException("The EL expression '" + m_expr + "' caused " + x, x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ValueBinding interface.								*/
	/*--------------------------------------------------------------*/
	public String getExpression() {
		return m_expr;
	}

	@Override
	public String toString() {
		return m_expr;
	}

	public NdBase getNode() {
		return m_node;
	}

	public MethodInvocator getInvocator(VariableResolver vr) throws ELException {
		if(!(m_node instanceof NdLookup))
			throw new ELException("The expression " + m_expr + " does not resolve as a method call.");

		//-- 2. We need to evaluate the base expression to find out.
		NdLookup l = (NdLookup) m_node;
		return l.getMethodInvocator(vr);
	}
}
