package to.etc.domui.component.plotly.traces;

import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-12-21.
 */
final public class PlLabelValueTrace extends AbstractLabelValueTrace<PlLabelValueTrace> implements IPlotlyTrace {
	@Override
	public void render(JsonBuilder b) throws Exception {
		renderBase(b);

		b.objArrayField("x");
		for(int i = 0; i < m_size; i++) {
			String l = m_labelAr[i];
			b.item(l);
		}
		b.arrayEnd();

		b.objArrayField("y");
		for(int i = 0; i < m_size; i++) {
			double l = m_valueAr[i];
			b.item(l);
		}
		b.arrayEnd();
	}

	public PlLabelValueTrace type(TraceType type) {
		super.setType(type);
		return this;
	}
}
