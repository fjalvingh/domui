package to.etc.pater;

final public class Pater {
	final static private ThreadLocal<IPaterContext> m_context = new ThreadLocal<IPaterContext>();

	private Pater() {}

	static public IPaterContext context() {
		IPaterContext c = m_context.get();
		if(null == c) {
			c = new DummyContext();
			m_context.set(c);
		}
		return c;
	}

	static public void set(IPaterContext context) {
		m_context.set(context);
	}
}
