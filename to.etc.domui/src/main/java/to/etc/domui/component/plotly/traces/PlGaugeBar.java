package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.layout.PlLine;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeBar {
	private String m_color;

	private final PlLine m_line = new PlLine();

	private double m_thickness;

	public void render(JsonBuilder b) throws Exception {
		PlotlyDataSet.renderColor(b, "color", m_color);
		if(! m_line.isEmpty()) {
			b.objObjField("line");
			m_line.render(b);
			b.objEnd();
		}
		if(m_thickness > 0.0)
			b.objField("thickness", m_thickness);
	}

	public boolean isEmpty() {
		return m_color == null
			&& m_line.isEmpty()
			&& m_thickness == 0.0d
			;
	}

	public PlGaugeBar color(String color) {
		m_color = color;
		return this;
	}

	public PlLine line() {
		return m_line;
	}

	/**
	 * Number between or equal to 0 and 1, sets the thickness of the
	 * bar as a fraction of the total thickness of the gauge.
	 */
	public PlGaugeBar thickness(double v) {
		m_thickness = v;
		return this;
	}
}
