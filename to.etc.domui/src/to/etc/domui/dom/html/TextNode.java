package to.etc.domui.dom.html;

import to.etc.domui.util.*;

public class TextNode extends NodeBase {
	private String m_text;

	/**
	 * Empty textnode constructor.
	 */
	public TextNode() {
		super("#text");
	}

	/**
	 * Create a TextNode for the given text, using tilde replacement. If the text starts with a tilde it
	 * is assumed to be a key in the page's resource bundle.
	 * @param text
	 */
	public TextNode(String text) {
		super("#text");
		m_text = text;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTextNode(this);
	}

	/**
	 * Returns the text as set by setText(), it does not do tilde replacement.
	 * @return
	 */
	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		if(DomUtil.isEqual(text, m_text))
			changed();
		m_text = text;
		if(getParent() != null) {
			getParent().childChanged();
			getParent().treeChanging();
			getParent().setMustRenderChildrenFully();
		}
	}

	/**
	 * Return the tilde-replaced text for this.
	 * @return
	 */
	public String getLiteralText() {
		return DomUtil.replaceTilded(this, m_text); // FIXME Performance?
	}
}
