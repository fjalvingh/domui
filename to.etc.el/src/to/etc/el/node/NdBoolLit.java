package to.etc.el.node;

public class NdBoolLit extends NdLiteral {
	static private NdBoolLit m_true = new NdBoolLit(Boolean.TRUE);

	static private NdBoolLit m_false = new NdBoolLit(Boolean.FALSE);

	private NdBoolLit(Boolean v) {
		super(v);
	}

	static public NdBoolLit getInstance(boolean type) {
		return type ? m_true : m_false;
	}
}
