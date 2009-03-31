package to.etc.domui.dom.html;

public class FileInput extends InputNodeBase {
	public FileInput() {
		super("input");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitFileInput(this);
	}
}
