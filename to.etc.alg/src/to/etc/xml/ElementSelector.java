package to.etc.xml;

import org.w3c.dom.*;

public class ElementSelector implements PathSelector {
	/** The element to uniquely match */
	private String	m_el;

	public ElementSelector(String el) {
		m_el = el;
	}

	public Node select(Node root, Node parent) throws Exception {
		return DomTools.nodeFind(parent, m_el);
	}

	@Override
	public String toString() {
		return m_el;
	}
}
