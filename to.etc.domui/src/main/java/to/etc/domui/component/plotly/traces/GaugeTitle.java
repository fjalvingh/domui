package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
final public class GaugeTitle {
	private String m_text;

	private PlGaugeAlign m_align;

	final private PlFont m_font = new PlFont();

	public void render(JsonBuilder b) throws IOException {
		PlGaugeAlign a = m_align;
		if(null != a)
			b.objField("align", a.name().toLowerCase());
		if(! m_font.isEmpty()) {
			b.objObjField("font");
			m_font.render(b);
			b.objEnd();
		}
		b.objFieldOpt("text", m_text);
	}

	public boolean isEmpty() {
		return m_align == null
			&& m_text == null
			&& m_font.isEmpty();
	}

	public String getText() {
		return m_text;
	}

	public GaugeTitle text(String text) {
		m_text = text;
		return this;
	}

	public PlGaugeAlign getAlign() {
		return m_align;
	}

	public GaugeTitle align(PlGaugeAlign align) {
		m_align = align;
		return this;
	}

	public PlFont font() {
		return m_font;
	}
}
