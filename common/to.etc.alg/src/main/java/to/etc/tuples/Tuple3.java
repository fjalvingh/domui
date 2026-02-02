package to.etc.tuples;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class Tuple3<A, B, C> {
	final private A		m_1;

	final private B		m_2;

	final private C		m_3;

	public Tuple3(A a, B b, C c) {
		m_1 = a;
		m_2 = b;
		m_3 = c;
	}

	public B get2() {
		return m_2;
	}

	public A get1() {
		return m_1;
	}

	public C get3() {
		return m_3;
	}
}
