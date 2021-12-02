package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlGaugeTrace extends AbstractBasicTrace<PlGaugeTrace> implements IPlotlyTrace {
	private final Set<PlIndicatorMode> m_mode = new HashSet<>();

	private double m_value;

	final private GaugeTitle m_title = new GaugeTitle();

	private PlGaugeAlign m_align;

	private final PlGaugeDetail m_gauge = new PlGaugeDetail();

	@Override
	public void render(@NonNull JsonBuilder b) throws Exception {
		renderBase(b);
		b.objField("type", "indicator");
		PlGaugeAlign a = m_align;
		if(null != a)
			b.objField("align", a.name().toLowerCase());
		b.objField("value", m_value);
		if(m_mode.size() > 0) {
			b.objField("mode", m_mode.stream().map(z -> z.name().toLowerCase()).collect(Collectors.joining("+")));
		}
		b.objObjField("gauge");
		m_gauge.render(b);
		b.objEnd();

		if(!m_title.isEmpty()) {
			b.objObjField("title");
			m_title.render(b);
			b.objEnd();
		}
	}

	public PlGaugeDetail gauge() {
		return m_gauge;
	}

	public GaugeTitle title() {
		return m_title;
	}

	public PlGaugeTrace value(double v) {
		m_value = v;
		return this;
	}

	/**
	 * Determines how the value is displayed on the graph. `number` displays the value
	 * numerically in text. `delta` displays the difference to a reference value in
	 * text. Finally, `gauge` displays the value graphically on an axis.
	 */
	public PlGaugeTrace mode(PlIndicatorMode... mode) {
		m_mode.addAll(Arrays.asList(mode));
		return this;
	}
}
