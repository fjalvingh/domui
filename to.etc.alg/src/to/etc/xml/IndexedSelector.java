package to.etc.xml;

import org.w3c.dom.*;

public class IndexedSelector implements PathSelector {
	/** The element name of the repeating thingy */
	private String	m_elem;

	/** The index of the repeat, starting at 0 */
	private int		m_index;

	public IndexedSelector(String elem, int index) {
		m_elem = elem;
		m_index = index;
	}


	public Node select(Node root, Node parent) throws Exception {
		//-- 1. Move to the nearest node
		NodeList nl = parent.getChildNodes();
		int len = nl.getLength();
		if(len < m_index) // Too small already-> exit,
			return null;
		int ct = 0;
		for(int i = 0; i < len; i++) // For all nodes,
		{
			Node n = nl.item(i); // Get item
			if(m_elem.equals(n.getNodeName())) // Is the name we need?
			{
				if(ct == m_index)
					return n; // Return if we're at the appropriate index
				ct++;
			}
		}
		return null; // Not enough occurences.
	}

	@Override
	public String toString() {
		return m_elem + "[" + m_index + "]";
	}
}
