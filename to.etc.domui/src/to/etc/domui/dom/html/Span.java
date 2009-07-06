package to.etc.domui.dom.html;

public class Span extends NodeContainer {
	public Span() {
		super("span");
	}

	public Span(String txt) {
		this();
		setText(txt);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitSpan(this);
	}

}
