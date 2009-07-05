package to.etc.domui.dom.html;

public class ATag extends NodeContainer {
	private String m_href;

	private String m_target;

	public ATag() {
		super("a");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitA(this);
	}

	public String getHref() {
		return m_href;
	}

	public void setHref(String href) {
		m_href = href;
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String target) {
		m_target = target;
	}
}
