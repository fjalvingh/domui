package to.etc.domui.component.plotly.traces;

import to.etc.domui.util.javascript.JsonBuilder;

/**
 * A dataset for a pie chart, consisting of String labels and a paired value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlPieTrace extends AbstractLabelValueTrace<PlPieTrace> {
	private double m_hole;

	private int m_domainX;

	private int m_domainY;

	private boolean m_hasDomain;

	public PlPieTrace() {
		setType(TraceType.Pie);
	}

	@Override
	public void render(JsonBuilder b) throws Exception {
		renderBase(b);

		b.objArrayField("labels");
		for(int i = 0; i < m_size; i++) {
			String l = m_labelAr[i];
			b.item(l);
		}
		b.arrayEnd();

		b.objArrayField("values");
		for(int i = 0; i < m_size; i++) {
			double l = m_valueAr[i];
			b.item(l);
		}
		b.arrayEnd();
		if(m_hole > 0.0D) {
			b.objField("hole", m_hole);
		}
		if(m_hasDomain) {
			b.objObjField("domain");
			b.objField("column", m_domainX);
			b.objField("row", m_domainY);
			b.objEnd();
		}
	}

	public PlPieTrace type(TraceType type) {
		super.setType(type);
		return this;
	}

	public double getHole() {
		return m_hole;
	}

	public int getDomainX() {
		return m_domainX;
	}

	public int getDomainY() {
		return m_domainY;
	}

	/**
	 * Set to a value between 0 and 1, this defines a hole inside the pie chart, turning
	 * it into a donut chart.
	 */
	public PlPieTrace hole(double hole) {
		m_hole = hole;
		return this;
	}

	public PlPieTrace domain(int x, int y) {
		m_domainX = x;
		m_domainY = y;
		m_hasDomain = true;
		return this;
	}
}
