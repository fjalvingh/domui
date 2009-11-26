package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

/**
 * Lookup a variable using the VariableResolver.
 *
 * @author jal
 * Created on May 17, 2005
 */
public class NdVarLookup extends NdBase {
	private String m_name;

	public NdVarLookup(String name) {
		m_name = name;
	}

	/**
	 * @see to.etc.el.node.NdBase#dump(to.etc.el.node.IndentWriter)
	 */
	@Override
	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName() + ": " + m_name);
	}

	public String getName() {
		return m_name;
	}

	/**
	 * Return the variable.
	 *
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		return vr.resolveVariable(m_name);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append(m_name);
	}
}
