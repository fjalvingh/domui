package to.etc.el.node;

import java.math.*;

import javax.servlet.jsp.el.*;

public class NdNotEqual extends NdComparatorOp {
	public NdNotEqual(NdBase a, NdBase b) {
		super(a, b);
	}

	@Override
	protected String getOperator() {
		return "!=";
	}

	@Override
	protected boolean apply(BigDecimal a, BigDecimal b) throws ELException {
		return a.compareTo(b) != 0;
	}

	@Override
	protected boolean apply(BigInteger a, BigInteger b) throws ELException {
		return a.compareTo(b) != 0;
	}

	@Override
	protected boolean apply(double a, double b) throws ELException {
		return a != b;
	}

	@Override
	protected boolean apply(long a, long b) throws ELException {
		return a != b;
	}
}
