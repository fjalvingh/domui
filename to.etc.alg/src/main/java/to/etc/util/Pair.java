package to.etc.util;

import java.util.Objects;

public class Pair<A, B> {
	final private A	m_1;

	final private B	m_2;

	public Pair(A one, B two) {
		m_1 = one;
		m_2 = two;
	}

	public A get1() {
		return m_1;
	}

	public B get2() {
		return m_2;
	}

	@Override public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(m_1, pair.m_1) &&
			Objects.equals(m_2, pair.m_2);
	}

	@Override public int hashCode() {
		return Objects.hash(m_1, m_2);
	}

	@Override public String toString() {
		return get1() + ", " + get2();
	}
}
