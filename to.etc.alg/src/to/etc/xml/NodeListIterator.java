package to.etc.xml;

import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

/**
 * Iterator for NodeList
 *
 * @author tinie
 * Created on 9/30/15.
 */
@DefaultNonNull
final public class NodeListIterator implements Iterable<Node>, Iterator<Node> {
	private final NodeList m_nodeList;

	private int m_index = 0;

	public NodeListIterator(NodeList nodeList) {
		m_nodeList = nodeList;
	}

	@Override public Iterator<Node> iterator() {
		return this;
	}

	@Override public boolean hasNext() {
		return m_index < m_nodeList.getLength();
	}

	@Override public Node next() {
		if(! hasNext())
			return null;
		return m_nodeList.item(m_index++);
	}

	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
