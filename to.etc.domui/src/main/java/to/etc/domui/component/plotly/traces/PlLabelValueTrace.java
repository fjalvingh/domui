package to.etc.domui.component.plotly.traces;

import to.etc.domui.util.javascript.JsonBuilder;

import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-12-21.
 */
final public class PlLabelValueTrace extends AbstractPlotlyTrace implements IPlotlyTrace {
	static private final int INITIAL_SIZE = 512;

	static private final int INCREMENT_SIZE = 512;

	/** Labels */
	private String[] m_labelAr = new String[INITIAL_SIZE];

	/** Values */
	private double[] m_valueAr = new double[INITIAL_SIZE];

	private int m_size;

	public PlLabelValueTrace() {
		m_traceMode = null;
	}

	public PlLabelValueTrace add(String label, double value) {
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
		return this;
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

	public PlLabelValueTrace mode(TraceMode mode) {
		m_traceMode = mode;
		return this;
	}

	public PlLabelValueTrace name(String name) {
		setName(name);
		return this;
	}

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
