package to.etc.tuples;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/26/14.
 */
public class Tuple2<A, B> {
	final private A		m_1;

	final private B		m_2;

	public Tuple2(A a, B b) {
		m_1 = a;
		m_2 = b;
	}

	public B get2() {
		return m_2;
	}

	public A get1() {
		return m_1;
	}
}
