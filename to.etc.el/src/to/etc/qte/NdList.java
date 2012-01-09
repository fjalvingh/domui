package to.etc.qte;

import javax.servlet.jsp.el.*;

/**
 * List of nodes to generate
 * <p>Created on Nov 25, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class NdList extends NdBase {
	private NdBase[] m_ar;

	public NdList(NdBase[] ar) {
		m_ar = ar;
	}

	public void generate(Appendable a, VariableResolver vr) throws Exception {
		for(NdBase b : m_ar)
			b.generate(a, vr);
	}
}
