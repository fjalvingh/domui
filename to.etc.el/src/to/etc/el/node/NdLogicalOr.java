package to.etc.el.node;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdLogicalOr extends NdBinary {
	public NdLogicalOr(NdBase a, NdBase b) {
		super(a, b);
	}

	@Override
	protected String getOperator() {
		return "&&";
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		Object a = m_a.evaluate(vr);
		boolean va = RuntimeConversions.convertToBool(a);
		if(va)
			return Boolean.TRUE; // If a is true the result is always true

		//-- A is false; the evaluation of B determines the result.
		a = m_b.evaluate(vr);
		return RuntimeConversions.convertToBooleanWrapper(a);
	}
}
