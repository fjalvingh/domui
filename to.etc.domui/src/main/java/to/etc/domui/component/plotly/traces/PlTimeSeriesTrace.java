package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.util.javascript.JsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * A trace (set of points) of [time, value] pairs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
@NonNullByDefault
final public class PlTimeSeriesTrace extends AbstractPlotlyTrace<PlTimeSeriesTrace> implements IPlotlyTrace {
	static private final int INITIAL_SIZE = 512;

	static private final int INCREMENT_SIZE = 512;

	/** Timestamps */
	private long[] m_timeAr = new long[INITIAL_SIZE];

	private double[] m_valueAr = new double[INITIAL_SIZE];

	private int m_size;

	private TimeSeriesType m_timeType = TimeSeriesType.Date;

	public PlTimeSeriesTrace add(long date, double value) {
		grow(1);
		int index = Arrays.binarySearch(m_timeAr, 0, m_size, date);
		if(index < 0) {
			index = -(index + 1);

			//-- Make room at this index
			for(int i = m_size; i > index; --i) {
				m_timeAr[i] = m_timeAr[i - 1];
				m_valueAr[i] = m_valueAr[i - 1];
			}
			m_timeAr[index] = date;
			m_valueAr[index] = value;
		} else {
			//-- Date exists; add the values
			m_valueAr[index] += value;
		}
		m_size++;
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

	public PlTimeSeriesTrace dateTime() {
		m_timeType = TimeSeriesType.DateTime;
		return this;
	}

	public PlTimeSeriesTrace date() {
		m_timeType = TimeSeriesType.Date;
		return this;
	}

	@Override
	public void render(JsonBuilder b) throws Exception {
		renderBase(b);

		b.objArrayField("x");
		DateFormat df = new SimpleDateFormat(m_timeType == TimeSeriesType.DateTime ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd");			// Format for plotly times
		for(int i = 0; i < m_size; i++) {
			long l = m_timeAr[i];
			b.item(df.format(l));
		}
		b.arrayEnd();

		b.objArrayField("y");
		for(int i = 0; i < m_size; i++) {
			double l = m_valueAr[i];
			b.item(l);
		}
		b.arrayEnd();
	}

	public PlTimeSeriesTrace type(TraceType type) {
		super.setType(type);
		return this;
	}
}
