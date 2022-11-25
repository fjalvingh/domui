package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeStep {
	private double m_from;

	private double m_to;

	private String m_color;

	public PlGaugeStep(double from, double to, String color) {
		m_from = from;
		m_to = to;
		m_color = color;
	}

	public void render(JsonBuilder b) throws Exception {
		b.objArrayField("range");
		b.item(m_from);
		b.item(m_to);
		b.arrayEnd();
		PlotlyDataSet.renderColor(b, "color", m_color);
	}
}
