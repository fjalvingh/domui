package to.etc.el.node;

import java.io.*;

abstract public class NdUnary extends NdBase {
	protected NdBase m_expr;

	public NdUnary(NdBase expr) {
		m_expr = expr;
	}

	/**
	 * @see to.etc.el.node.NdBase#dump(to.etc.el.node.IndentWriter)
	 */
	@Override
	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName() + " on expression:");
		w.inc();
		m_expr.dump(w);
		w.dec();
	}


}
