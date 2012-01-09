package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdUnaryNot extends NdUnary {
	public NdUnaryNot(NdBase expr) {
		super(expr);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("!");
		m_expr.getExpression(a);
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		return Boolean.valueOf(!RuntimeConversions.convertToBool(a));
	}
}
