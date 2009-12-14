package to.etc.domui.dom.html;

import to.etc.domui.util.*;

public class ATag extends NodeContainer {
	private String m_href;

	private String m_target;

	public ATag() {
		super("a");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitA(this);
	}

	public String getHref() {
		return m_href;
	}

	public void setHref(String href) {
		if(DomUtil.isEqual(m_href, href))
			return;
		m_href = href;
		changed();
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String target) {
		if(DomUtil.isEqual(m_target, target))
			return;
		m_target = target;
		changed();
	}
}
