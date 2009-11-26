package to.etc.xml;

import java.util.*;

import org.w3c.dom.*;

/**
 * Encapsulates a path into an XML expression. It consists of
 * a set of PathSelector nodes in a list that get executed in
 * order to get to a given value within an XML structure. The
 * matcher code is not very efficient as it traverses the
 * DOM from start to finish every time a value needs to be got.
 *
 * <p>Created on May 23, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class PathExpression {
	private PathSelector[]	m_path;

	private String			m_s;

	private PathExpression(PathSelector[] par, String s) {
		m_s = s;
		m_path = par;
	}

	/**
	 * Converts a path selection string into a path expression.
	 * @param s
	 * @return
	 * @throws Exception
	 */
	static public PathExpression getExpression(String s) throws Exception {
		ArrayList al = new ArrayList(); // The list of selectors built.

		//-- First: does the string start with '/'? In that case it is a root selector.
		if(s.startsWith("/")) {
			//-- Append the root selector first
			s = s.substring(1);
			al.add(new RootSelector());
		}

		//-- Ok, traverse the rest.
		StringTokenizer st = new StringTokenizer(s, "/"); // Each expression separated by '/'
		while(st.hasMoreTokens()) {
			String sel = st.nextToken();
			if(sel.length() != 0) // Replace // with /
				al.add(decodeSelector(sel)); // Decode the selector and add to the path
		}
		return new PathExpression((PathSelector[]) al.toArray(new PathSelector[al.size()]), s);
	}

	@Override
	public String toString() {
		return m_s;
	}

	/**
	 * Decodes the selector type and returns it.
	 * @param s
	 * @return
	 * @throws Exception
	 */
	static private PathSelector decodeSelector(String s) throws Exception {
		int pos = s.indexOf('['); // Is it an indexed selector?
		if(pos != -1) {
			int epos = s.lastIndexOf(']'); // Find terminating ]
			if(epos < pos)
				throw new IllegalStateException("The selector " + s + " is invalid (bad [] placement)");
			if(epos != s.length() - 1) // Must be last char in name
				throw new IllegalStateException("The '[]' index selector MUST be the last thing in a selector string");

			//-- The thingy between it must be integer
			String b = s.substring(pos + 1, epos); // Get $
			int ix;
			try {
				ix = Integer.parseInt(b);
			} catch(Exception x) {
				throw new IllegalStateException("The selector " + s + " has an invalid number in the index expression");
			}

			//-- Now check the element name
			String el = s.substring(0, pos); // All before the [] expr
			if(el.startsWith("@"))
				throw new IllegalStateException("An attribute cannot be used with an indexing selector: attributes are unique, Einstein!");
			return new IndexedSelector(el, ix);
		}

		if(s.equalsIgnoreCase("#text")) // Text selector?
			return new TextSelector();

		if(s.startsWith("@")) // attribute selector?
			return new AttrSelector(s.substring(1));

		return new ElementSelector(s);
	}

	public final Node getNode(Node root, Node parent, StringBuffer error) throws Exception {
		//-- Just walk the selector array to get a node expression. This returns null on error.
		for(int i = 0; i < m_path.length; i++) {
			PathSelector ps = m_path[i];
			Node n = ps.select(root, parent);
			if(n == null) {
				//-- No match! Report an error location,
				error.setLength(0);
				error.append("No match for selector '");
				for(int j = 0; j <= i; j++) {
					if(j > 0)
						error.append("/");
					error.append(m_path[j].toString());
				}
				error.append("'");
				return null;
			}
			parent = n;
		}
		return parent;
	}


}
