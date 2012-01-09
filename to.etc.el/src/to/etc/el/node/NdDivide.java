package to.etc.el.node;

import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

public class NdDivide extends NdBinary {

	/**
	 * @param a
	 * @param b
	 */
	public NdDivide(NdBase a, NdBase b) {
		super(a, b);
	}

	/**
	 * See jsp 2.0, JSP2.3.5.2
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

		/*
		 * If a or b is BigDecimal or BigInteger coerce both to BigDecimal
		 */
		if(cla == BigDecimal.class)
			return ((BigDecimal) a).divide(ElUtil.coerceToBigDecimal(b), BigDecimal.ROUND_HALF_UP);
		if(clb == BigDecimal.class)
			return ElUtil.coerceToBigDecimal(a).divide((BigDecimal) b, BigDecimal.ROUND_HALF_UP);
		if(cla == BigInteger.class || b == BigInteger.class)
			return ElUtil.coerceToBigDecimal(a).divide(ElUtil.coerceToBigDecimal(b), BigDecimal.ROUND_HALF_UP);

		/*
		 * otherwise coerce to double and apply
		 */
		try {
			return WrapperCache.getDouble(ElUtil.coerceToDouble(a) / ElUtil.coerceToDouble(b));
		} catch(Exception x) {
			x.printStackTrace();
			throw new ELException("Division by zero in EL expression");
		}
	}

	@Override
	protected String getOperator() {
		return "/";
	}

}
