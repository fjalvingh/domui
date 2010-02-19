package to.etc.domui.dom.html;

/**
 * This special TextNode is treated by DomUI as a normal TextNode, but with one exception: it's content
 * is not normal text but XML. When rendered this node does not escape anything, so tags in here are
 * rendered as tags that fall "outside" the DomUI DOM tree. This can be used when strict formatting
 * is needed, or when the overhead for a DOM tree for a part is too big. In addition, this will also
 * prevent any indenting from taking place inside this node, so space-perfect rendering can be done,
 * for instance inside pre-like blocks.
 * Disadvantage is that when the content changes it is replaced in it's entirery.
 *
 * <p>This mostly replaces the LiteralXhtml tag because it is more useful: it does not need any
 * enclosing tag but can be used everywhere.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
public class XmlTextNode extends TextNode {
	public XmlTextNode() {}

	public XmlTextNode(String txt) {
		setText(txt);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitXmlNode(this);
	}
}
