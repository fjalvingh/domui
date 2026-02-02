package to.etc.xml;

import java.util.*;

import org.w3c.dom.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/16/15.
 */
final public class NodeIterator implements Iterable<Node>, Iterator<Node> {
	private final Node m_node;

	private int m_index = 0;

	public NodeIterator(Node node) {
		m_node = node;
	}

	@Override public Iterator<Node> iterator() {
		return this;
	}

	@Override public boolean hasNext() {
		return m_index < m_node.getChildNodes().getLength();
	}

	@Override public Node next() {
		if(! hasNext())
			return null;
		return m_node.getChildNodes().item(m_index++);
	}

	@Override public void remove() {
		throw new UnsupportedOperationException();
	}
}
