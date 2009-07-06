package to.etc.domui.dom.html;

public class Li extends NodeContainer {
	public Li() {
		super("li");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitLi(this);
	}
}
