package to.etc.domui.dom.html;

import to.etc.webapp.nls.*;

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
	public void visit(INodeVisitor v) throws Exception {
		v.visitTHead(this);
	}

	public TH[] setHeaders(String... labels) {
		forceRebuild();
		TR row = new TR();
		add(row);
		TH[] res = new TH[labels.length];
		int ix = 0;
		for(String s : labels) {
			TH th = new TH();
			row.add(th);
			th.setText(s);
			res[ix++] = th;
		}
		return res;
	}

	public TH[] setHeaders(BundleRef b, String... keys) {
		forceRebuild();
		TR row = new TR();
		add(row);
		TH[] res = new TH[keys.length];
		int ix = 0;
		for(String s : keys) {
			TH th = new TH();
			row.add(th);
			th.setText(b.getString(s));
			res[ix++] = th;
		}
		return res;
	}

	public void setHeaderCssClasses(String cls) {
		for(TH th : getChildren(TH.class))
			th.setCssClass(cls);
	}

	private TR getRow() {
		for(int i = getChildCount(); --i >= 0;) {
			NodeBase b = getChild(i);
			if(b instanceof TR)
				return (TR) b;
		}
		TR tr = new TR();
		add(tr);
		return tr;
	}

	public TH addHeader(String text) {
		TR tr = getRow();
		TH th = new TH();
		tr.add(th);
		th.setText(text);
		return th;
	}
}
