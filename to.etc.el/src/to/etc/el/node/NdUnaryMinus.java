package to.etc.el.node;

import java.io.*;
import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

public class NdUnaryMinus extends NdUnary {
	public NdUnaryMinus(NdBase b) {
		super(b);
	}

	/**
	 * See JSP 2.0, JSP 2.3.5.4
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_expr.evaluate(vr);
		if(a == null)
			return WrapperCache.getLong(0);
		//		Class cl	= a.getClass();
		if(a instanceof BigDecimal)
			return ((BigDecimal) a).negate();
		if(a instanceof BigInteger)
			return ((BigInteger) a).negate();

		if(a instanceof String) {
			if(ElUtil.isFloatingPointString(a))
				return Double.valueOf(-ElUtil.coerceToDouble(a));
			return Long.valueOf(-ElUtil.coerceToLong(a));
		}
		if(a instanceof Double)
			return WrapperCache.getDouble(-((Double) a).doubleValue());
		if(a instanceof Float)
			return WrapperCache.getFloat(-((Float) a).floatValue());
		if(a instanceof Long)
			return WrapperCache.getLong(-((Long) a).longValue());
		if(a instanceof Integer)
			return WrapperCache.getInteger(-((Integer) a).intValue());
		if(a instanceof Short)
			return WrapperCache.getShort((short) -((Short) a).shortValue());
		if(a instanceof Byte)
			return WrapperCache.getByte((byte) -((Byte) a).byteValue());
		throw new ELException("Unary minus operator not applicable to operand " + a);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append('-');
		a.append('(');
		m_expr.getExpression(a);
		a.append(')');
	}

}
