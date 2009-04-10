package to.etc.domui.dom.html;

/**
 * A THEAD node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
public class THead extends NodeContainer {
	public THead() {
		super("thead");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitTHead(this);
	}
	public void	setHeaders(String... labels) {
		forceRebuild();
		TR	row	= new TR();
		add(row);
		for(String s: labels) {
			TH	th = new TH();
			row.add(th);
			th.setLiteralText(s);
		}
	}
	private TR	getRow() {
		for(int i = getChildCount(); --i >= 0;) {
			NodeBase b = getChild(i);
			if(b instanceof TR)
				return (TR)b;
		}
		TR	tr = new TR();
		add(tr);
		return tr;
	}

	public TH	addHeader(String text) {
		TR	tr = getRow();
		TH	th	= new TH();
		tr.add(th);
		th.setLiteralText(text);
		return th;
	}
}
