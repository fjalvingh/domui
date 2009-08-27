package to.etc.xml;

import org.w3c.dom.*;

/**
 * This selector merely returns the root node of a path expression.
 * 
 * <p>Created on May 23, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class RootSelector implements PathSelector {
	public Node select(Node root, Node parent) throws Exception {
		return root;
	}

	@Override
	public String toString() {
		return "/";
	}
}
