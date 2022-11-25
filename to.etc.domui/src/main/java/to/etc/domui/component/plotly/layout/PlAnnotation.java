package to.etc.domui.component.plotly.layout;

import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlAnnotation {
	private PlFont m_font = new PlFont();

	private boolean m_showArrow;

	private String m_text;

	private double m_x;

	private double m_y;

	public PlAnnotation(double x, double y, String text) {
		m_text = text;
		m_x = x;
		m_y = y;
	}

	public PlFont font() {
		return m_font;
	}

	public boolean isShowArrow() {
		return m_showArrow;
	}

	public PlAnnotation showArrow(boolean showArrow) {
		m_showArrow = showArrow;
		return this;
	}

	public PlAnnotation showArrow() {
		m_showArrow = true;
		return this;
	}

	public String getText() {
		return m_text;
	}

	//public PlAnnotation text(String text) {
	//	m_text = text;
	//	return null;
	//}

	public double getX() {
		return m_x;
	}

	//public PlAnnotation setX(double x) {
	//	m_x = x;
	//}

	public double getY() {
		return m_y;
	}

	//public PlAnnotation setY(double y) {
	//	m_y = y;
	//}

	public void render(JsonBuilder b) throws IOException {
		if(! m_font.isEmpty()) {
			b.objObjField("font");
			m_font.render(b);
			b.objEnd();
		}
		b.objField("x", m_x);
		b.objField("y", m_y);
		b.objField("text", m_text);
		b.objField("showarrow", m_showArrow);
	}
}
