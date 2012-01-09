package to.etc.util;

import java.util.*;

/**
 *
 * Created on Apr 25, 2003
 * @author jal
 */
public class StringPair {
	private String				m_a, m_b;

	static public Comparator	A_COMPARATOR	= new Comparator() {
													public int compare(Object a, Object b) {
														if(a instanceof StringPair) {
															if(b instanceof StringPair)
																return ((StringPair) a).getA().compareTo(((StringPair) b).getA());
															if(b instanceof String)
																return ((StringPair) a).getA().compareTo((String) b);
														}
														if(b instanceof StringPair) {
															if(a instanceof String) {
																return ((StringPair) b).getA().compareTo((String) a);
															}
														}
														return -1;
													}
												};


	public StringPair(String a, String b) {
		m_a = a;
		m_b = b;
	}

	public StringPair() {
	}

	static private boolean eql(String s1, String s2) {
		if(s1 == s2)
			return true; // Both same / null
		if(s1 == null || s2 == null)
			return false; // One is null but not both
		return s1.equals(s2);
	}


	@Override
	public boolean equals(Object parm1) {
		if(!(parm1 instanceof StringPair))
			return false;
		StringPair s = (StringPair) parm1;
		if(!eql(s.m_a, m_a))
			return false;
		return eql(s.m_b, m_b);
	}

	@Override
	public int hashCode() {
		int rv = 0;
		if(m_a != null)
			rv += m_a.hashCode();
		if(m_b != null)
			rv += m_b.hashCode();
		return rv;
	}

	@Override
	public String toString() {
		return "(" + m_a + "," + m_b + ")";
	}

	/**
	 * @return
	 */
	public String getA() {
		return m_a;
	}

	/**
	 * @return
	 */
	public String getB() {
		return m_b;
	}

	/**
	 * @param string
	 */
	public void setA(String string) {
		m_a = string;
	}

	/**
	 * @param string
	 */
	public void setB(String string) {
		m_b = string;
	}

}
