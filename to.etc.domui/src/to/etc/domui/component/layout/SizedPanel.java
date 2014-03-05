package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * A side panel with a top, bottom and variable-size middle area, and optional sizes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2013
 */
public class SizedPanel extends Div {
	private Div m_top;

	private Div m_middle;

	private Div m_bottom;

	private Caption2 m_caption;

	public SizedPanel() {
	}

	public SizedPanel(int width, int height) {
		setCssClass("ui-szdp");
		if(width > 0)
			setWidth(width + "px");
		if(height > 0)
			setHeight(height + "px");
	}

	@Nonnull
	public Caption2 caption() {
		Caption2 cap = m_caption;
		if(null == cap) {
			cap = m_caption = new Caption2(CaptionType.Panel);
			top().add(0, cap);
		}
		return cap;
	}

	public void setCaption(@Nonnull String title) {
		caption().setCaption(title);
	}

	@Override
	protected void createFrame() throws Exception {
		delegateTo(middle());
	}

	@Override
	public void onBeforeFullRender() throws Exception {
		int height = DomUtil.pixelSize(getHeight());
		if(height > 0)
			JavascriptUtil.setThreePanelHeight(createStatement(), m_top, middle(), m_bottom);
	}

	@Nonnull
	public Div top() {
		Div t = m_top;
		if(null == t) {
			t = m_top = new Div();
			undelegatedAdd(0, t);
		}
		return t;
	}

	@Nonnull
	public Div bottom() {
		Div t = m_bottom;
		if(null == t) {
			t = m_bottom = new Div();
			undelegatedAdd(999, t);
		}
		return t;
	}

	@Nonnull
	public Div middle() {
		Div t = m_middle;
		if(null == t) {
			t = m_middle = new Div();
			int ix = 0;
			if(m_top != null)
				ix = 1;

			add(ix, t);
		}
		return t;
	}
}
