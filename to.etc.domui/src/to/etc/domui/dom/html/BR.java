package to.etc.domui.dom.html;

public class BR extends NodeBase {
	public BR() {
		super("br");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitBR(this);
	}
}
