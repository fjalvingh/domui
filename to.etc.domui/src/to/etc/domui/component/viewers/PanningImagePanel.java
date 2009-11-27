package to.etc.domui.component.viewers;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

/**
 * This shows a bitmapped image and allows it to be dragged etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2009
 */
public class PanningImagePanel extends Div {
	private Img m_img = new Img();

	private Img m_panUp, m_panDown, m_panLeft, m_panRight;

	public PanningImagePanel() {
		setCssClass("ui-pip-img");
	}

	public void setImageURL(String url) {
		m_img.setSrc(url);
	}

	@Override
	public void createContent() throws Exception {
		setWidth("500px");
		setHeight("700px"); // FIXME size needs to be defined dynamically.
		add(m_img);

		m_panLeft = new Img("THEME/pan-left.png");
		add(m_panLeft);

		m_panRight = new Img("THEME/pan-right.png");
		add(m_panRight);

		m_panUp = new Img("THEME/pan-up.png");
		add(m_panUp);

		m_panDown = new Img("THEME/pan-down.png");
		add(m_panDown);

		setBase(m_panDown, m_panLeft, m_panRight, m_panUp);
		m_panUp.setTop(0);
		m_panUp.setLeft(240);

	}

	static private void setBase(Img... iar) {
		for(Img i : iar) {
			i.setPosition(PositionType.RELATIVE);
			i.setBackgroundColor("transparant");
			i.setZIndex(10);
		}
	}

}
