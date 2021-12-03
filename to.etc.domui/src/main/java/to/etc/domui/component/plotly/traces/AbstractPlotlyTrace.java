package to.etc.domui.component.plotly.traces;

import to.etc.domui.component.plotly.layout.PlLine;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
abstract class AbstractPlotlyTrace<T extends AbstractPlotlyTrace<T>> extends AbstractBasicTrace<T> {
	protected TraceMode m_traceMode = TraceMode.Lines;

	protected TraceType m_type = TraceType.Scatter;

	private PlLine m_line = new PlLine();

	private PlFillMode m_fill;

	private String m_stackGroup;

	public TraceMode getTraceMode() {
		return m_traceMode;
	}

	public void setTraceMode(TraceMode traceMode) {
		m_traceMode = traceMode;
	}

	public TraceType getType() {
		return m_type;
	}

	public void setType(TraceType type) {
		m_type = type;
	}

	@Override
	protected void renderBase(JsonBuilder b) throws IOException {
		super.renderBase(b);
		if(null != m_traceMode) {
			switch(m_traceMode) {
				default:
					break;

				case Lines:
					b.objField("mode", "lines");
					break;

				case Markers:
					b.objField("mode", "markers");
					break;

				case MarkersAndLines:
					b.objField("mode", "markers+lines");
					break;
			}
			switch(m_type) {
				default:
					break;

				case Scatter:
					b.objField("type", "scatter");
					break;
			}
		}

		if(m_name != null)
			b.objField("name", m_name);

		if(m_type != null) {
			b.objField("type", m_type.name().toLowerCase());
		}
		if(m_fill != null) {
			b.objField("fill", m_fill.name().toLowerCase());
		}
		String g = m_stackGroup;
		if(null != g) {
			b.objField("stackgroup", g);
		}

		if(! m_line.isEmpty()) {
			b.objObjField("line");
			m_line.render(b);
			b.objEnd();
		}
	}

	/**
	 * Accesses the line object to set trace graphical presentation like color and width.
	 */
	public PlLine line() {
		return m_line;
	}

	public T fill(PlFillMode mode) {
		m_fill = mode;
		return (T) this;
	}

	public T stackGroup(String g) {
		m_stackGroup = g;
		return (T) this;
	}
}
