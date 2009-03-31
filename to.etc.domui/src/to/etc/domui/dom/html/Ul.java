package to.etc.domui.dom.html;

/**
 * Represents an UL node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class Ul extends NodeContainer {
	public Ul() {
		super("ul");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitUl(this);
	}
}
