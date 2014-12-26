package to.etc.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class Tuple2<A, B> {
	final private A		m_a;

	final private B		m_b;

	public Tuple2(A a, B b) {
		m_a = a;
		m_b = b;
	}

	public B getB() {
		return m_b;
	}

	public A getA() {
		return m_a;
	}
}
