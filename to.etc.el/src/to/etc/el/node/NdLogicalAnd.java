package to.etc.el.node;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

public class NdLogicalAnd extends NdBinary {
	public NdLogicalAnd(NdBase a, NdBase b) {
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
		if(!va)
			return Boolean.FALSE; // If a is false the result is always false.

		//-- A is true; the evaluation of B determines the result.
		a = m_b.evaluate(vr);
		return RuntimeConversions.convertToBooleanWrapper(a);
	}
}
