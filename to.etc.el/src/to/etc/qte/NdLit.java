package to.etc.qte;

import javax.servlet.jsp.el.*;

public class NdLit extends NdBase {
	private String m_data;

	NdLit(String msg) {
		m_data = msg;
	}

	public void generate(Appendable a, VariableResolver vr) throws Exception {
		a.append(m_data);
	}
}
