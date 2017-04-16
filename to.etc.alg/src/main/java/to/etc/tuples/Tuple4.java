package to.etc.tuples;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class Tuple4<A, B, C, D> {
	final private A		m_1;

	final private B		m_2;

	final private C		m_3;

	final private D	m_4;

	public Tuple4(A a, B b, C c, D d) {
		m_1 = a;
		m_2 = b;
		m_3 = c;
		m_4 = d;
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

	public D get4() {
		return m_4;
	}
}
