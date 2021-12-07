package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeDetail {
	private final PlGaugeAxis m_axis = new PlGaugeAxis();

	private final PlGaugeBar m_bar = new PlGaugeBar();

	private final PlGaugeThreshold m_threshold = new PlGaugeThreshold();

	private String m_bgColor;

	private int m_borderWidth;

	private String m_borderColor;

	final private List<PlGaugeStep> m_stepList = new ArrayList<>();

	public void render(JsonBuilder b) throws Exception {
		if(! m_axis.isEmpty()) {
			b.objObjField("axis");
			m_axis.render(b);
			b.objEnd();
		}
		if(! m_bar.isEmpty()) {
			b.objObjField("bar");
			m_bar.render(b);
			b.objEnd();
		}
		if(! m_threshold.isEmpty()) {
			b.objObjField("threshold");
			m_threshold.render(b);
			b.objEnd();
		}
		PlotlyDataSet.renderColor(b, "bgcolor", m_bgColor);
		PlotlyDataSet.renderColor(b, "bordercolor", m_borderColor);
		if(m_borderWidth > 0)
			b.objField("borderwidth", m_borderWidth);
		if(m_stepList.size() > 0) {
			b.objArrayField("steps");
			for(PlGaugeStep step : m_stepList) {
				b.itemObj();
				step.render(b);
				b.objEnd();
			}
			b.arrayEnd();
		}
	}

	public PlGaugeAxis axis() {
		return m_axis;
	}

	public PlGaugeBar bar() {
		return m_bar;
	}

	public PlGaugeThreshold threshold() {
		return m_threshold;
	}

	public PlGaugeDetail bgColor(String bgColor) {
		m_bgColor = bgColor;
		return this;
	}

	public PlGaugeDetail borderWidth(int borderWidth) {
		m_borderWidth = borderWidth;
		return this;
	}

	public PlGaugeDetail borderColor(String borderColor) {
		m_borderColor = borderColor;
		return this;
	}

	public PlGaugeDetail step(double from, double to, String color) {
		m_stepList.add(new PlGaugeStep(from, to, color));
		return this;
	}
}
