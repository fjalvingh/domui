package to.etc.domui.dom.html;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-7-17.
 */
public class Para extends NodeContainer {
	public Para() {
		super("p");
	}

	public Para(String css) {
		super("p");
		setCssClass(css);
	}

	public Para(String cssClass, String text) {
		this(cssClass);
		setText(text);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitP(this);
	}
}
