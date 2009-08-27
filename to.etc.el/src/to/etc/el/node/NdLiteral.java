package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

/**
 * Any kind of literal.
 * 
 * Created on May 18, 2005
 * @author jal
 */
public class NdLiteral extends NdBase {
	/** The literal value */
	private Object m_value;

	public NdLiteral(Object val) {
		m_value = val;
	}

	@Override
	final public Object evaluate(VariableResolver vr) throws ELException {
		return m_value;
	}

	/**
	 * @see to.etc.el.node.NdBase#dump(to.etc.el.node.IndentWriter)
	 */
	@Override
	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName() + ": " + m_value);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append(m_value.toString());
	}
}
