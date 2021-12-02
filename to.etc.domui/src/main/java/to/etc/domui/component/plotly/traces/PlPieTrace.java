package to.etc.domui.component.plotly.traces;

import to.etc.domui.util.javascript.JsonBuilder;

/**
 * A dataset for a pie chart, consisting of String labels and a paired value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlPieTrace extends AbstractLabelValueTrace<PlPieTrace> {
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
	}

	public PlPieTrace type(TraceType type) {
		super.setType(type);
		return this;
	}
}
