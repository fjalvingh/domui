package to.etc.domui.dom.html;

/**
 * The 'u' html tag.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 3, 2011
 */
public class Underline extends NodeContainer {
	public Underline() {
		super("u");
	}

	/**
	 * Create a span holding the specified message.
	 * @param txt
	 */
	public Underline(String txt) {
		this();
		setText(txt);
	}

	/**
	 * Create a span holding a specific message and having a given css class.
	 * @param cssClass
	 * @param text
	 */
	public Underline(String cssClass, String text) {
		this(text);
		setCssClass(cssClass);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitUnderline(this);
	}
}
