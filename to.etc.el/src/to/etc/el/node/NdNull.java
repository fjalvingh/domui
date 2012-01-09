package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

public class NdNull extends NdBase {
	static private final NdNull m_inst = new NdNull();

	static public NdNull getInstance() {
		return m_inst;
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		return null;
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		a.append("null");
	}

}
