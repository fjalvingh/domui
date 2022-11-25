package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeAxis {
	private double m_rangeFrom;

	private double m_rangeTo;

	private PlFont m_tickFont = new PlFont();

	private String m_tickColor;

	private double m_tickAngle;

	private int m_nTicks;

	private boolean m_separateThousands;

	private PlInsideOutside m_ticks;

	private int m_tickWidth;

	public void render(JsonBuilder b) throws Exception {
		if(m_rangeFrom != 0.0 || m_rangeTo != 0.0) {
			b.objArrayField("range");
			b.item(m_rangeFrom);
			b.item(m_rangeTo);
			b.arrayEnd();
		}
		if(!m_tickFont.isEmpty()) {
			b.objObjField("tickfont");
			m_tickFont.render(b);
			b.objEnd();
		}
		PlotlyDataSet.renderColor(b, "tickcolor", m_tickColor);
		if(m_tickAngle != 0.0d)
			b.objField("tickangle", m_tickAngle);
		if(m_nTicks > 0)
			b.objField("nticks", m_nTicks);
		if(m_separateThousands)
			b.objField("separatethousands", true);
		PlInsideOutside t = m_ticks;
		if(null != t)
			b.objField("ticks", t.name().toLowerCase());
		if(m_tickWidth > 0)
			b.objField("tickwidth", m_tickWidth);

	}

	public boolean isEmpty() {
		return m_rangeFrom <= 0.0
			&& m_rangeTo <= 0.0
			&& m_tickFont.isEmpty()
			&& m_tickColor == null
			&& m_tickAngle == 0.0d
			&& m_nTicks == 0
			&& !m_separateThousands
			&& m_ticks == null
			&& m_tickWidth == 0;
	}

	public PlGaugeAxis range(double from, double to) {
		m_rangeFrom = from;
		m_rangeTo = to;
		return this;
	}

	public PlFont tickFont() {
		return m_tickFont;
	}

	public PlGaugeAxis tickColor(String tickColor) {
		m_tickColor = tickColor;
		return this;
	}

	public PlGaugeAxis tickAngle(double tickAngle) {
		m_tickAngle = tickAngle;
		return this;
	}

	public PlGaugeAxis nTicks(int nTicks) {
		m_nTicks = nTicks;
		return this;
	}

	public PlGaugeAxis setSeparateThousands() {
		m_separateThousands = true;
		return this;
	}

	public PlGaugeAxis ticks(PlInsideOutside ticks) {
		m_ticks = ticks;
		return this;
	}

	public PlGaugeAxis tickWidth(int tickWidth) {
		m_tickWidth = tickWidth;
		return this;
	}
}
