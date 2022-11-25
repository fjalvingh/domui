package to.etc.domui.component.plotly.traces;

import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
abstract public class AbstractLabelValueTrace<T extends AbstractLabelValueTrace<T>> extends AbstractPlotlyTrace<T> implements IPlotlyTrace {
	static private final int INITIAL_SIZE = 512;

	static private final int INCREMENT_SIZE = 512;

	/** Labels */
	protected String[] m_labelAr = new String[INITIAL_SIZE];

	/** Values */
	protected double[] m_valueAr = new double[INITIAL_SIZE];

	protected int m_size;

	public AbstractLabelValueTrace() {
		m_traceMode = null;
	}

	public T add(String label, double value) {
		grow(1);
		int index = Arrays.binarySearch(m_labelAr, 0, m_size, label);
		if(index < 0) {
			index = -(index + 1);

			//-- Make room at this index
			for(int i = m_size; i > index; --i) {
				m_labelAr[i] = m_labelAr[i - 1];
				m_valueAr[i] = m_valueAr[i - 1];
			}
			m_labelAr[index] = label;
			m_valueAr[index] = value;
		} else {
			//-- Label exists; add the values
			m_valueAr[index] += value;
		}
		m_size++;
		return (T) this;
	}

	private void grow(int count) {
		int nl = m_labelAr.length + count;
		if(nl < m_size) {
			return;
		}
		int nwsize = ((nl + INCREMENT_SIZE - 1) / INCREMENT_SIZE) * INCREMENT_SIZE;
		String[] nLabel = new String[nwsize];
		System.arraycopy(m_labelAr, 0, nLabel, 0, m_size);
		m_labelAr = nLabel;

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

	public T mode(TraceMode mode) {
		m_traceMode = mode;
		return (T) this;
	}
}
