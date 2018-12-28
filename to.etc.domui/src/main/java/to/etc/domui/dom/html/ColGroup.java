package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-12-18.
 */
final public class ColGroup extends NodeContainer {
	private int m_span;

	public ColGroup() {
		super("colgroup");
	}

	public ColGroup(String cssClasses) {
		this();
		addCssClass(cssClasses);
	}

	@Override public void visit(@NonNull INodeVisitor v) throws Exception {
		v.visitColGroup(this);
	}

	public int getSpan() {
		return m_span;
	}

	public void setSpan(int span) {
		if(span == m_span)
			return;
		m_span = span;
		changed();
	}
}
