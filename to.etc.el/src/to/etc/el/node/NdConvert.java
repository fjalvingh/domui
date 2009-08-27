package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdConvert extends NdUnary {
	private Class m_tocl;

	public NdConvert(NdBase b, Class tocl) {
		super(b);
		m_tocl = tocl;
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		return RuntimeConversions.convertTo(a, m_tocl);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("(cast-to ");
		a.append(m_tocl.getName());
		a.append(")");
	}
}
