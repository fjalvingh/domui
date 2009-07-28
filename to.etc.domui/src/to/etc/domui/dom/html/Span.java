package to.etc.domui.dom.html;

/**
 * A SPAN tag. This is the base for all inline components.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2009
 */
public class Span extends NodeContainer {
	public Span() {
		super("span");
	}

	/**
	 * Create a span holding the specified message.
	 * @param txt
	 */
	public Span(String txt) {
		this();
		setText(txt);
	}

	/**
	 * Create a span holding a specific message and having a given css class.
	 * @param cssClass
	 * @param text
	 */
	public Span(String cssClass, String text) {
		this(text);
		setCssClass(cssClass);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitSpan(this);
	}
}
