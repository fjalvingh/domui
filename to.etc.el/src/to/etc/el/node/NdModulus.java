package to.etc.el.node;

import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

public class NdModulus extends NdBinary {
	/**
	 * @param a
	 * @param b
	 */
	public NdModulus(NdBase a, NdBase b) {
		super(a, b);
	}

	/**
	 * See jsp 2.0, JSP2.3.5.3
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_a.evaluate(vr);
		Object b = m_b.evaluate(vr);

		if(a == null && b == null) // If a and b both null return Long(0)
			return WrapperCache.getLong(0);
		Class cla = a == null ? null : a.getClass();
		Class clb = b == null ? null : b.getClass();

		/**
		 * if a or b is bigdecimal, float, double or string looking like # convert
		 * to double and apply.
		 */
		if(isFloaty(cla) || isFloaty(clb) || ElUtil.isFloatingPointString(a) || ElUtil.isFloatingPointString(b))
			WrapperCache.getDouble(ElUtil.coerceToDouble(a) % ElUtil.coerceToDouble(b));

		/*
		 * If either is BigInteger coerce both to that
		 */
		if(cla == BigInteger.class)
			return ((BigInteger) a).remainder(ElUtil.coerceToBigInteger(b));
		if(clb == BigInteger.class)
			return ElUtil.coerceToBigInteger(a).remainder((BigInteger) b);

		return WrapperCache.getLong(ElUtil.coerceToLong(a) % ElUtil.coerceToLong(b));
	}

	static private boolean isFloaty(Class cl) {
		return cl == BigDecimal.class || cl == Double.class || cl == Float.class;
	}

	@Override
	protected String getOperator() {
		return "%";
	}

}
