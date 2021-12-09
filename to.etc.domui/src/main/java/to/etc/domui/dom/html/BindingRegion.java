package to.etc.domui.dom.html;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-21.
 */
public class BindingRegion extends Div implements IBindingRegion {
	public BindingRegion() {
	}

	public BindingRegion(String css) {
		super(css);
	}

	public BindingRegion(NodeBase... children) {
		super(children);
	}

	public BindingRegion(String css, String text) {
		super(css, text);
	}
}
