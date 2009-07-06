package to.etc.domui.dom.html;

public class HTag extends NodeContainer {
	public HTag(int level) {
		super("H" + level);
		if(level < 0 || level > 6)
			throw new IllegalStateException("Invalid H tag");
	}

	public HTag(int level, String txt) {
		this(level);
		setText(txt);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitH(this);
	}
}
