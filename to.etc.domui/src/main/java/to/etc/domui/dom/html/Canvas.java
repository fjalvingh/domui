package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-10-21.
 */
public class Canvas extends NodeContainer {
	public Canvas() {
		super("canvas");
	}

	@Override
	public void visit(@NonNull INodeVisitor v) throws Exception {
		v.visitCanvas(this);
	}
}
