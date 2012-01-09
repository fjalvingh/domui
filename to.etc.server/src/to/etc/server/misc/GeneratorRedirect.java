package to.etc.server.misc;

import to.etc.server.servlet.*;

/**
 * Created on Feb 4, 2005
 * @author jal
 */
public class GeneratorRedirect implements AbstractGenerator {
	/** The redirect target */
	private String	m_target;

	public GeneratorRedirect(String target) {
		m_target = target;
	}

	/**
	 * Just output the redirect.
	 */
	public void generate(ContextServletContext ctx) throws Exception {
		ctx.getResponse().sendRedirect(m_target);
	}

	public long getLastModified() {
		return -1;
	}
}
