package to.etc.xml;

import org.w3c.dom.*;

public class AttrSelector implements PathSelector {
	/** The name of the attribute to match */
	private String	m_name;

	public AttrSelector(String name) {
		m_name = name;
	}

	public Node select(Node root, Node parent) throws Exception {
		NamedNodeMap map = parent.getAttributes();
		return map.getNamedItem(m_name);
	}

	@Override
	public String toString() {
		return "@" + m_name;
	}

}
