package to.etc.domui.dom.html;

public class Pre extends Div {
	public Pre() {
		internalSetTag("pre");
	}

	public Pre(String cssClass) {
		super(cssClass);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitPre(this);
	}
}
