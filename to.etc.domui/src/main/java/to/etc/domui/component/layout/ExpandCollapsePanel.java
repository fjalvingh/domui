package to.etc.domui.component.layout;

import com.google.common.base.Objects;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TextNode;

/**
 * A panel with a title bar whose content can be expanded and collapsed. The content
 * lives inside the panel, so hiding or showing it changes nothing around the panel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 04-06-20.
 */
public class ExpandCollapsePanel extends Div {
	private NodeBase m_label;

	private NodeBase m_content;

	private boolean m_expanded;

	public ExpandCollapsePanel() {
		super("ui-expcp");
	}

	public ExpandCollapsePanel(String label) {
		this();
		setLabel(label);
	}

	public ExpandCollapsePanel(String label, String content) {
		this();
		setLabel(label);
		setContent(content);
	}

	public ExpandCollapsePanel(String label, NodeContainer content) {
		this();
		setLabel(label);
		setContent(content);
	}

	@Override
	public void createContent() throws Exception {
		Div header = new Div("ui-expcp-hdr");
		add(header);
		header.add(new LinkButton("", m_expanded ? Icon.faMinus : Icon.faPlus, a -> toggle()));
		header.add(" ");
		NodeBase label = m_label;
		if(null != label)
			header.add(label);

		if(m_expanded) {
			Div contentDiv = new Div("ui-expcp-c");
			add(contentDiv);
			expandContent(contentDiv);
		}
	}

	private void toggle() {
		m_expanded = !m_expanded;
		forceRebuild();
	}

	/**
	 * Fill the content area, which is only present while the panel is expanded. Override
	 * to calculate the content at the moment it becomes visible.
	 */
	protected void expandContent(Div contentDiv) throws Exception {
		NodeBase content = m_content;
		if(null != content)
			contentDiv.add(content);
	}

	public void expand() {
		if(isCollapsed()) {
			m_expanded = true;
			forceRebuild();
		}
	}

	public void collapse() {
		if(!isCollapsed()) {
			m_expanded = false;
			forceRebuild();
		}
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
		forceRebuild();
	}

	public void setLabel(String name) {
		setLabel(new TextNode(name));
	}

	public void setContent(String name) {
		setContent(new TextNode(name));
	}

	public boolean isInitiallyExpanded() {
		return m_expanded;
	}

	public void setInitiallyExpanded(boolean initiallyExpanded) {
		if(m_expanded == initiallyExpanded)
			return;
		m_expanded = initiallyExpanded;
		forceRebuild();
	}

	public boolean isCollapsed() {
		return !m_expanded;
	}
}
