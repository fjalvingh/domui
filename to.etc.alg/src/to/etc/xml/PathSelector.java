package to.etc.xml;

import org.w3c.dom.*;

/**
 * This is a simple path selector base class.
 * <p>Created on May 23, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public interface PathSelector {
	public Node select(Node root, Node parent) throws Exception;
}
