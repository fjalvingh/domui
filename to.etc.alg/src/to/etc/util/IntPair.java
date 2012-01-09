package to.etc.util;

public class IntPair {
	private int	m_a;

	private int	m_b;

	public IntPair(int a, int b) {
		m_a = a;
		m_b = b;
	}

	public int a() {
		return m_a;
	}

	public int b() {
		return m_b;
	}

	public void setA(int a) {
		m_a = a;
	}

	public void setB(int b) {
		m_b = b;
	}

	public void set(int a, int b) {
		m_a = a;
		m_b = b;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof IntPair) || o == null)
			return false;
		return m_a == ((IntPair) o).m_a && m_b == ((IntPair) o).m_b;
	}

	@Override
	public int hashCode() {
		return m_a + m_b;
	}

	@Override
	public String toString() {
		return "(" + a() + "," + b() + ")";
	}
}
