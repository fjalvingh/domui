package to.etc.el.node;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.jsp.el.*;

public class NdEmpty extends NdUnary {
	public NdEmpty(NdBase expr) {
		super(expr);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("empty (");
		m_expr.getExpression(a);
		a.append(")");
	}

	/**
	 * As per JSP 2.0 std, JSP2.3.7
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		if(a == null)
			return Boolean.TRUE;
		if((a instanceof String) && ((String) a).length() == 0)
			return Boolean.TRUE;
		Class cla = a.getClass();
		if(cla.isArray()) {
			if(Array.getLength(a) == 0)
				return Boolean.TRUE;
		}
		if(a instanceof Map && ((Map) a).size() == 0)
			return Boolean.TRUE;
		if(a instanceof Collection && ((Collection) a).size() == 0)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
}
