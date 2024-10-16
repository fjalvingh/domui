package to.etc.domui.component.layout;

import com.google.common.base.Objects;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TextNode;

/**
 * A panel with a title bar whose content can be expanded and collapsed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 04-06-20.
 */
public class ExpandCollapsePanel extends Span {
	private NodeBase m_label;

	private NodeBase m_content;

	private final Div m_contentDiv = new Div();

	private boolean m_isInitiallyExpanded;

	private LinkButton m_lb = new LinkButton("", a -> toggle());

	public ExpandCollapsePanel() {
	}

	public ExpandCollapsePanel(String label) {
		setLabel(label);
	}

	public ExpandCollapsePanel(String label, String content) {
		setLabel(label);
		setContent(content);
	}

	public ExpandCollapsePanel(String label, NodeContainer content) {
		setLabel(label);
		setContent(content);
	}

	@Override
	final public void createContent() throws Exception {
		if(m_isInitiallyExpanded) {
			expandPanel();
		} else {
			m_lb.setImage(Icon.faPlus);
		}
		add(m_lb);
		add("\u00a0");
		add(m_label);
	}

	private void toggle() {
		if(m_contentDiv.isAttached()) {
			collapsePanel();
		} else {
			expandPanel();
		}
	}

	private void collapsePanel() {
		m_contentDiv.remove();
		m_lb.setImage(Icon.faPlus);
	}

	private void expandPanel() {
		m_lb.setImage(Icon.faMinus);
		m_contentDiv.removeAllChildren();
		appendAfterMe(m_contentDiv);
		expandContent(m_contentDiv);
	}

	public void expand() {
		if(isCollapsed()) {
			expandPanel();
		}
	}

	public void collapse() {
		if(!isCollapsed())
			collapsePanel();
	}

	protected void expandContent(Div contentDiv) {
		m_contentDiv.add(m_content);
	}

	public NodeBase getLabel() {
		return m_label;
	}

	public void setLabel(NodeBase label) {
		if(label == m_label)
			return;
		m_label = label;
		forceRebuild();
	}

	public NodeBase getContent() {
		return m_content;
	}

	public void setContent(NodeBase content) {
		if(Objects.equal(m_content, content))
			return;
		m_content = content;
	}

	public void setLabel(String name) {
		setLabel(new TextNode(name));
	}

	public void setContent(String name) {
		setContent(new TextNode(name));
	}

	public boolean isInitiallyExpanded() {
		return m_isInitiallyExpanded;
	}

	public void setInitiallyExpanded(boolean initiallyExpanded) {
		m_isInitiallyExpanded = initiallyExpanded;
	}

	public boolean isCollapsed() {
		return !m_contentDiv.isAttached();
	}
}
