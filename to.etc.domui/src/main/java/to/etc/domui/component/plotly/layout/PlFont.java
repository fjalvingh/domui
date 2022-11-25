package to.etc.domui.component.plotly.layout;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlFont {
	private String m_family;

	private int m_size;

	private String m_color;

	public String getFamily() {
		return m_family;
	}

	public PlFont family(String family) {
		m_family = family;
		return this;
	}

	public int getSize() {
		return m_size;
	}

	public PlFont size(int size) {
		m_size = size;
		return this;
	}

	public String getColor() {
		return m_color;
	}

	public PlFont color(String color) {
		m_color = color;
		return this;
	}

	public boolean isEmpty() {
		return m_color == null
			&& m_size == 0
			&& m_family == null;
	}

	public void render(JsonBuilder b) throws IOException {
		b.objFieldOpt("family", m_family);
		if(m_size > 0)
			b.objField("size", m_size);
		PlotlyDataSet.renderColor(b, "color", m_color);
	}
}
