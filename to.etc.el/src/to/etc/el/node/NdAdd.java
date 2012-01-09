package to.etc.el.node;

import java.math.*;

import javax.servlet.jsp.el.*;

public class NdAdd extends NdBinOp {
	/**
	 * @param a
	 * @param b
	 */
	public NdAdd(NdBase a, NdBase b) {
		super(a, b);
	}

	@Override
	protected Object apply(BigDecimal a, BigDecimal b) throws ELException {
		return a.add(b);
	}

	@Override
	protected Object apply(BigInteger a, BigInteger b) throws ELException {
		return a.add(b);
	}

	@Override
	protected double apply(double a, double b) throws ELException {
		return a + b;
	}

	@Override
	protected long apply(long a, long b) throws ELException {
		return a + b;
	}

	@Override
	protected String getOperator() {
		return "+";
	}
}
