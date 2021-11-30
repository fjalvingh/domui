package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.Date;

/**
 * A trace (set of points) of [time, value] pairs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
@NonNullByDefault
final public class PlTimeSeriesTrace extends AbstractPlotlyTrace implements IPlotlyTrace {
	static private final int INITIAL_SIZE = 512;

	static private final int INCREMENT_SIZE = 512;

	/** Timestamps */
	private long[] m_timeAr = new long[INITIAL_SIZE];

	private double[] m_valueAr = new double[INITIAL_SIZE];

	private int m_size;

	public PlTimeSeriesTrace add(long date, double value) {
		grow(1);
		int ix = m_size++;
		m_valueAr[ix] = value;
		m_timeAr[ix] = date;
		return this;
	}

	public PlTimeSeriesTrace add(Date date, double value) {
		add(date.getTime(), value);
		return this;
	}

	private void grow(int count) {
		int nl = m_timeAr.length + count;
		if(nl < m_size) {
			return;
		}
		int nwsize = ((nl + INCREMENT_SIZE - 1) / INCREMENT_SIZE) * INCREMENT_SIZE;
		long[] ntime = new long[nwsize];
		System.arraycopy(m_timeAr, 0, ntime, 0, m_size);
		m_timeAr = ntime;

		double[] nval = new double[nwsize];
		System.arraycopy(m_valueAr, 0, nval, 0, m_size);
		m_valueAr = nval;
	}

	public void clear() {
		m_size = 0;
	}

	public int getSize() {
		return m_size;
	}

	public PlTimeSeriesTrace mode(TraceMode mode) {
		m_traceMode = mode;
		return this;
	}

	public PlTimeSeriesTrace name(String name) {
		setName(name);
		return this;
	}

	@Override
	public void render(JsonBuilder b) throws Exception {
		b.obj();
		renderBase(b);

		b.objEnd();
	}
}
