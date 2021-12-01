package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.plotly.layout.PlLine;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
abstract class AbstractPlotlyTrace {
	protected TraceMode m_traceMode = TraceMode.Lines;

	@Nullable
	protected String m_name;

	protected TraceType m_type = TraceType.Scatter;

	private PlLine m_line = new PlLine();

	public TraceMode getTraceMode() {
		return m_traceMode;
	}

	public void setTraceMode(TraceMode traceMode) {
		m_traceMode = traceMode;
	}

	@Nullable
	public String getName() {
		return m_name;
	}

	public void setName(@Nullable String name) {
		m_name = name;
	}

	public TraceType getType() {
		return m_type;
	}

	public void setType(TraceType type) {
		m_type = type;
	}

	protected void renderBase(JsonBuilder b) throws Exception {
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
}
