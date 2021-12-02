package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A dataset for a pie chart, consisting of String labels and a paired value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlPieTrace extends AbstractLabelValueTrace<PlPieTrace> {
	private double m_hole;

	private PlTextPosition m_textPosition;

	private PlTextOrientation m_insideTextOrientation;

	private PlFont m_insideTextFont = new PlFont();

	private PlFont m_outsideTextFont = new PlFont();

	private boolean m_autoMargin;

	private Set<PlTextInfo> m_textInfo = new HashSet<>();

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
		PlTextPosition t = m_textPosition;
		if(null != t) {
			b.objField("textposition", t.name().toLowerCase());
		}
		if(m_autoMargin)
			b.objField("automargin", true);
		PlTextOrientation or = m_insideTextOrientation;
		if(null != or)
			b.objField("insidetextorientation", or.name().toLowerCase());
		if(!m_insideTextFont.isEmpty()) {
			b.objObjField("insidetextfont");
			m_insideTextFont.render(b);
			b.objEnd();
		}
		if(!m_outsideTextFont.isEmpty()) {
			b.objObjField("outsidetextfont");
			m_outsideTextFont.render(b);
			b.objEnd();
		}
		if(m_textInfo.size() > 0) {
			if(m_textInfo.contains(PlTextInfo.None)) {
				b.objField("textinfo", "none");
			} else {
				b.objField("textinfo", m_textInfo.stream().map(a -> a.name().toLowerCase()).collect(Collectors.joining("+")));
			}
		}
	}

	public PlPieTrace type(TraceType type) {
		super.setType(type);
		return this;
	}

	public double getHole() {
		return m_hole;
	}

	/**
	 * Set to a value between 0 and 1, this defines a hole inside the pie chart, turning
	 * it into a donut chart.
	 */
	public PlPieTrace hole(double hole) {
		m_hole = hole;
		return this;
	}

	public PlTextPosition getTextPosition() {
		return m_textPosition;
	}

	/**
	 * Specifies the location of the `textinfo`.
	 */
	public PlPieTrace textPosition(PlTextPosition textPosition) {
		m_textPosition = textPosition;
		return this;
	}

	public boolean isAutoMargin() {
		return m_autoMargin;
	}

	/**
	 * Determines whether outside text labels can push the margins.
	 */
	public PlPieTrace autoMargin() {
		m_autoMargin = true;
		return this;
	}

	public PlTextOrientation getInsideTextOrientation() {
		return m_insideTextOrientation;
	}

	/**
	 * Controls the orientation of the text inside chart sectors. When set to "auto", text
	 * may be oriented in any direction in order to be as big as possible in the middle of
	 * a sector. The "horizontal" option orients text to be parallel with the bottom of the
	 * chart, and may make text smaller in order to achieve that goal. The "radial" option
	 * orients text along the radius of the sector. The "tangential" option orients text
	 * perpendicular to the radius of the sector.
	 */
	public PlPieTrace insideTextOrientation(PlTextOrientation insideTextOrientation) {
		m_insideTextOrientation = insideTextOrientation;
		return this;
	}

	/**
	 * Sets the font used for `textinfo` lying inside the sector.
	 */
	public PlFont insideTextFont() {
		return m_insideTextFont;
	}

	/**
	 * Sets the font used for `textinfo` lying outside the sector.
	 */
	public PlFont outsideTextFont() {
		return m_outsideTextFont;
	}

	/**
	 * Determines which trace information appear on the graph. Can be any combination
	 * of the TextInfo enum.
	 */
	public PlPieTrace textInfo(PlTextInfo... ti) {
		m_textInfo.addAll(Arrays.asList(ti));
		return this;
	}
}
