package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.layout.PlLine;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeThreshold {
	private final PlLine m_line = new PlLine();

	private double m_thickness;

	private double m_value = Double.MIN_VALUE;

	public void render(JsonBuilder b) throws Exception {
		if(m_value != Double.MIN_VALUE)
			b.objField("value", m_value);
		if(! m_line.isEmpty()) {
			b.objObjField("line");
			m_line.render(b);
			b.objEnd();
		}
		if(m_thickness > 0.0)
			b.objField("thickness", m_thickness);
	}

	public boolean isEmpty() {
		return m_value == Double.MIN_VALUE
			&& m_line.isEmpty()
			&& m_thickness == 0.0d
			;
	}

	public PlGaugeThreshold value(double v) {
		m_value = v;
		return this;
	}

	public PlLine line() {
		return m_line;
	}

	/**
	 * Number between or equal to 0 and 1, sets the thickness of the
	 * bar as a fraction of the total thickness of the gauge.
	 */
	public PlGaugeThreshold thickness(double v) {
		m_thickness = v;
		return this;
	}
}
