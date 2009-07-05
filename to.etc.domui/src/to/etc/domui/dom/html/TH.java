package to.etc.domui.dom.html;

import to.etc.domui.util.*;

/**
 * The TH node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 2, 2008
 */
public class TH extends NodeContainer {
	private String m_scope = "col";

	private int m_colspan = -1;

	public TH() {
		super("th");
	}

	@Override
	public void visit(NodeVisitor v) throws Exception {
		v.visitTH(this);
	}

	public String getScope() {
		return m_scope;
	}

	public void setScope(String scope) {
		if(DomUtil.isEqual(scope, m_scope))
			changed();
		m_scope = scope;
	}

	public int getColspan() {
		return m_colspan;
	}

	public void setColspan(int colspan) {
		m_colspan = colspan;
	}
}
