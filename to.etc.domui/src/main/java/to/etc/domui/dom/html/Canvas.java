package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-10-21.
 */
public class Canvas extends NodeContainer {
	private int m_width = -1;

	private int m_height = -1;

	public Canvas() {
		super("canvas");
	}

	public Canvas(int width, int height) {
		super("canvas");
		m_height = height;
		m_width = width;
	}

	@Override
	public void visit(@NonNull INodeVisitor v) throws Exception {
		v.visitCanvas(this);
	}

	public int getCanvasWidth() {
		return m_width;
	}

	public int getCanvasHeight() {
		return m_height;
	}
}
