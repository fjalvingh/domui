package to.etc.domui.dom.html;

public class Label extends NodeContainer {
	private NodeBase				m_forNode;

	private String					m_for;

	public Label() {
		super("label");
	}
	public Label(String text) {
		super("label");
		setButtonText(text);
	}
	public Label(NodeBase fr, String text) {
		super("label");
		setButtonText(text);
		setForNode(fr);
	}
	public Label(String text, String cssClass) {
		this();
		setButtonText(text);
		setCssClass(cssClass);
	}
	public Label(NodeBase fr, String text, String cssClass) {
		super("label");
		setButtonText(text);
		setForNode(fr);
		setCssClass(cssClass);
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitLabel(this);
	}
	public NodeBase getForNode() {
		return m_forNode;
	}
	public void setForNode(NodeBase forNode) {
		m_forNode = forNode;
	}
	public String getFor() {
		if(m_forNode != null)
			return m_forNode.getActualID();
		return m_for;
	}

	public void setFor(String for1) {
		m_for = for1;
	}
}
