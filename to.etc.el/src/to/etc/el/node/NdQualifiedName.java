package to.etc.el.node;

import java.io.*;

import javax.servlet.jsp.el.*;

public class NdQualifiedName extends NdBase {
	private String m_a;

	private String m_b;

	public NdQualifiedName(String pkg, String name) {
		m_a = pkg;
		m_b = name;
	}

	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		throw new IllegalStateException("Qualified name");
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		if(m_a != null) {
			a.append(m_a);
			a.append(':');
		}
		a.append(m_b);
	}

	public String getNS() {
		return m_a;
	}

	public String getName() {
		return m_b;
	}
}
