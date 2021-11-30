package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
abstract class AbstractPlotlyTrace {
	protected TraceMode m_traceMode = TraceMode.Lines;

	@Nullable
	protected String m_name;

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
}
