package to.etc.domui.component.panellayout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Experimental: a panel that handles component layout for things added to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class LayoutPanelBase extends Div implements ILayoutPanel {
	@Nonnull
	private ILayoutManager m_layout;

	public LayoutPanelBase() {
		this(new XYLayout());
	}

	public LayoutPanelBase(@Nonnull ILayoutManager layout) {
		m_layout = layout;
	}

	@Nonnull
	@Override
	public ILayoutManager getLayout() {
		return m_layout;
	}

	public void setLayout(@Nonnull ILayoutManager layout) {
		m_layout = layout;
	}

	public void add(@Nonnull NodeBase node, @Nonnull Object layoutOptions) {
		getLayout().place(this, node, layoutOptions);
	}


}
