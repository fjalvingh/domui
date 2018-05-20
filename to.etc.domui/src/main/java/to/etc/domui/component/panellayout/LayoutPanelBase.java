package to.etc.domui.component.panellayout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;

/**
 * Experimental: a panel that handles component layout for things added to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class LayoutPanelBase extends Div implements ILayoutPanel {
	@NonNull
	private ILayoutManager m_layout;

	public LayoutPanelBase() {
		this(new XYLayout());
	}

	public LayoutPanelBase(@NonNull ILayoutManager layout) {
		m_layout = layout;
	}

	@NonNull
	@Override
	public ILayoutManager getLayout() {
		return m_layout;
	}

	public void setLayout(@NonNull ILayoutManager layout) {
		m_layout = layout;
	}

	public void add(@NonNull NodeBase node, @NonNull Object layoutOptions) {
		getLayout().place(this, node, layoutOptions);
	}


}
