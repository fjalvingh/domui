package to.etc.el.node;

import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.util.*;

/**
 * A binary arithmetic operation: +, -, *
 *
 * @author jal
 * Created on May 18, 2005
 */
abstract public class NdBinOp extends NdBinary {
	/**
	 * @param a
	 * @param b
	 */
	public NdBinOp(NdBase a, NdBase b) {
		super(a, b);
	}

	abstract protected Object apply(BigDecimal a, BigDecimal b) throws ELException;

	abstract protected Object apply(BigInteger a, BigInteger b) throws ELException;

	abstract protected double apply(double a, double b) throws ELException;

	abstract protected long apply(long a, long b) throws ELException;

	/**
	 * Handles all coerces for arithmetic operations as described in
	 * JSP 2.0 standard JSP2.3.5.1
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

		//-- If either class is BigDecimal coerce the other to BigDecimal and operate
		if(cla == BigDecimal.class)
			return apply((BigDecimal) a, ElUtil.coerceToBigDecimal(b));
		if(clb == BigDecimal.class)
			return apply(ElUtil.coerceToBigDecimal(a), (BigDecimal) b);

		/*
		 * If either is a float, double or "String containing '.', e or E then
		 * 	if the other is BigInteger coerce both to BigInteger
		 * 	else coerce to double
		 */
		if(cla == Float.class || cla == Double.class || ElUtil.isFloatingPointString(a)) {
			if(clb == BigInteger.class) // If the other is BigInteger
				return apply(ElUtil.coerceToBigInteger(a), (BigInteger) b);
			return new Double(apply(ElUtil.coerceToDouble(a), ElUtil.coerceToDouble(b)));
		}
		if(clb == Float.class || clb == Double.class || ElUtil.isFloatingPointString(b)) {
			if(cla == BigInteger.class) // If the other is BigInteger
				return apply((BigInteger) a, ElUtil.coerceToBigInteger(b));
			return new Double(apply(ElUtil.coerceToDouble(a), ElUtil.coerceToDouble(b)));
		}

		/*
		 * If a or b is BigInteger coerce both to BigInteger
		 */
		if(cla == BigInteger.class)
			return apply((BigInteger) a, ElUtil.coerceToBigInteger(b));
		if(clb == BigInteger.class)
			return apply(ElUtil.coerceToBigInteger(a), (BigInteger) b);

		/*
		 * coerce both to long and handle
		 */
		return WrapperCache.getLong(apply(ElUtil.coerceToLong(a), ElUtil.coerceToLong(b)));
	}
}
