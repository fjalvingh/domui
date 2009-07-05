package to.etc.domui.dom.html;

public class Span extends NodeContainer {
	public Span() {
		super("span");
	}

	public Span(String txt) {
		this();
		setLiteralText(txt);
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitSpan(this);
	}

}
