package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

public class NdPropertyBase extends NdBase {
	static private final NdPropertyBase m_instance = new NdPropertyBase();

	static final public NdPropertyBase getInstance() {
		return m_instance;
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		return vr.resolveVariable(null);
	}

	@Override
	public void getExpression(Appendable a) throws IOException {}
}
