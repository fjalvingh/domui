package to.etc.xml;

import org.w3c.dom.*;

/**
 * Matches a single #text node. The node cannot contain anything else but #text nodes.
 * <p>Created on Jun 9, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TextSelector implements PathSelector {
	public TextSelector() {
	}

	public Node select(Node root, Node parent) throws Exception {
		NodeList nl = parent.getChildNodes();
		if(nl.getLength() == 0)
			return null;
		parent.normalize();
		if(nl.getLength() != 1)
			return null;

		//-- Only text nodes and PI nodes?
		for(int i = nl.getLength(); --i >= 0;) {
			Node n = nl.item(i);
			if(n.getNodeType() != Node.TEXT_NODE) {
				if(n.getNodeType() == Node.ELEMENT_NODE)
					return null;
			}
		}
		return nl.item(0);
	}

	@Override
	public String toString() {
		return "#text";
	}
}
