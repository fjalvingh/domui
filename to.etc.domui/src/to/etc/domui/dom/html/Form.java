package to.etc.domui.dom.html;

public class Form extends NodeContainer {
	private String		m_method;
	private String		m_enctype;
	private String		m_action;
	private String		m_target;

	public Form() {
		super("form");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitForm(this);
	}

	public String getMethod() {
		return m_method;
	}

	public void setMethod(String method) {
		m_method = method;
	}

	public String getEnctype() {
		return m_enctype;
	}

	public void setEnctype(String enctype) {
		m_enctype = enctype;
	}

	public String getAction() {
		return m_action;
	}

	public void setAction(String action) {
		m_action = action;
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String target) {
		m_target = target;
	}
}
