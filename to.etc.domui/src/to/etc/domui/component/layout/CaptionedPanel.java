package to.etc.domui.component.layout;

import to.etc.domui.dom.html.*;

/**
 * A panel with a beveled caption above it. The caption defaults to some text but can be
 * anything actually. Both the title and the content of this panel can be changed and can
 * contain any other node structure. Simple constructors exist to quickly render a panel
 * around a structure. The parameterless constructor expects the title of the panel to
 * be set separately; the content is initially created as a Div, and can either be replaced
 * or it can be added to.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class CaptionedPanel extends Div {
	private NodeContainer		m_contentContainer;
	private NodeContainer		m_titleContainer;

	/**
	 * Create a panel with the specified String title and a content node.
	 * @param title
	 * @param content
	 */
	public CaptionedPanel(String title, NodeContainer content) {
		this(new TextNode(title), content);
	}
	public CaptionedPanel(NodeContainer title, NodeContainer content) {
		setCssClass("ui-pnl-outer");
		m_titleContainer = title;
		m_titleContainer.addCssClass("ui-pnl-caption");
		m_contentContainer = content;
		m_contentContainer.addCssClass("ui-pnl-cont");
	}

	/**
	 * Create a panel with both the title and the container as a Node structure.
	 * @param title
	 * @param content
	 */
	public CaptionedPanel(NodeBase title, NodeContainer content) {
		this(new Div(), content);
		m_titleContainer.add(title);
	}

	/**
	 * Create a panel with a title and an empty Div as the container.
	 * @param title
	 */
	public CaptionedPanel(String title) {
		this(new TextNode(title), new Div());
	}

	/**
	 * Create an empty panel without a title and with an empty Div as the content node.
	 */
	public CaptionedPanel() {
		this(new Div(), new Div());
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(m_titleContainer);
		add(m_contentContainer);
	}

	/**
	 * Return the current content container; it can be added to.
	 * @return
	 */
	public NodeContainer getContent() {
		return m_contentContainer;
	}
	/**
	 * Get the current title container.
	 * @return
	 */
	public NodeContainer getTitleContainer() {
		return m_titleContainer;
	}

	/**
	 * Set the title for this panel as a String. This replaces the current node
	 * with a Div(TextNode) node.
	 * @see to.etc.domui.dom.html.NodeBase#setTitle(java.lang.String)
	 */
	@Override
	public void	setTitle(String txt) {
		getTitleContainer().setLiteralText(txt);
	}

	/**
	 * Replaces the current title container with a different one.
	 * @param c
	 */
	public void	setTitleContainer(NodeContainer c) {
		m_titleContainer.remove();
		m_titleContainer = c;
		add(0, c);
		m_titleContainer.addCssClass("ui-pnl-caption");
	}

	/**
	 * Replaces the current content container with a different one.
	 * @param c
	 */
	public void	setContentContainer(NodeContainer c) {
		m_contentContainer.remove();
		m_contentContainer = c;
		add(1, c);
		m_contentContainer.addCssClass("ui-pnl-cont");
	}
}
