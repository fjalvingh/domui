package to.etc.util;

/**
 * Base implementation of a maker which has a single integer key
 * and the class type as match value.
 *
 * @author jal
 * Created on May 12, 2004
 */
abstract public class IntKeyAndMakerBase implements iKeyAndMaker {
	abstract public Object makeObject() throws Exception;

	private int	m_key;

	public IntKeyAndMakerBase(int val) {
		m_key = val;
	}

	@Override
	public boolean equals(Object a) {
		if(a == null) // Empty?
			return false;
		if(a.getClass() != getClass()) // Not same class?
			return false;

		return ((IntKeyAndMakerBase) a).m_key == m_key;
	}

	@Override
	public int hashCode() {
		return (getClass().hashCode() << 12) ^ m_key;
	}

	public int getKey() {
		return m_key;
	}
}
