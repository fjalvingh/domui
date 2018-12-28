package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-12-18.
 */
public class Col extends NodeBase {
	public Col() {
		super("col");
	}

	public Col(String cssClasses) {
		this();
		addCssClass(cssClasses);
	}

	@Override public void visit(@NonNull INodeVisitor v) throws Exception {
		v.visitCol(this);
	}
}
