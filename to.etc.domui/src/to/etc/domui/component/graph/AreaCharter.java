package to.etc.domui.component.graph;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jCharts.axisChart.*;
import org.jCharts.chartData.*;
import org.jCharts.properties.*;
import org.jCharts.types.*;

/**
 * Helps constructing an area graph.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class AreaCharter implements ICharterHelper {
	private JGraphChartSource m_source;

	private String m_title;

	private String m_bucketTitle;

	private int m_width, m_height;

	private String m_valueTitle;

	private AreaChartProperties m_areaProperties = new AreaChartProperties();

	private ChartProperties m_properties = new ChartProperties();

	private AxisProperties m_axisProperties = new AxisProperties();

	private LegendProperties m_legendProperties = new LegendProperties();

	@SuppressWarnings("unchecked")
	// FIXME Cannot create ANY construct here that does NOT cause generic warnings. Good job, Sun.
	static public class BucketData {
		private String m_bucketName;

		private Comparable m_bucketValue;

		private List<Double> m_values = new ArrayList<Double>();

		protected BucketData(String bucketName, Comparable< ? > bucketValue) {
			m_bucketName = bucketName;
			m_bucketValue = bucketValue;
		}

		public void setValue(int index, double val) {
			while(m_values.size() <= index)
				m_values.add(null);
			m_values.set(index, Double.valueOf(val));
		}

		public String getBucketName() {
			return m_bucketName;
		}

		public Comparable getBucketValue() {
			return m_bucketValue;
		}

		public Double getValue(int ix) {
			if(ix >= m_values.size())
				return null;
			return m_values.get(ix);
		}
	}

	public class DataSet {
		private String m_name;

		private int m_index;

		private Paint m_paint;

		protected DataSet(Paint paint, String name, int index) {
			m_paint = paint;
			m_name = name;
			m_index = index;
		}

		public void add(String bucketlabel, Comparable< ? > sortvalue, double value) {
			_add(m_index, bucketlabel, sortvalue, value);
		}

		public String getName() {
			return m_name;
		}

		public Paint getPaint() {
			return m_paint;
		}
	}

	private Map<Object, BucketData> m_bucketSet = new HashMap<Object, BucketData>();

	private List<DataSet> m_sets = new ArrayList<DataSet>();

	protected AreaCharter(JGraphChartSource source, String title, int width, int height, String bucketTitle, String valueTitle) {
		m_source = source;
		m_title = title;
		m_width = width;
		m_height = height;
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
	}

	public DataSet addDataSet(Paint paint, String name) {
		DataSet ds = new DataSet(paint, name, m_sets.size());
		m_sets.add(ds);
		return ds;
	}

	void _add(int index, String bucketlabel, Comparable< ? > sortvalue, double value) {
		BucketData bd = addBucket(bucketlabel, sortvalue); // Add/get bucket.
		bd.setValue(index, value);
	}

	public BucketData addBucket(String bucketlabel, Comparable< ? > sortvalue) {
		BucketData d = m_bucketSet.get(sortvalue);
		if(d == null) {
			d = new BucketData(bucketlabel, sortvalue);
			m_bucketSet.put(sortvalue, d);
		}
		return d;
	}

	public List<BucketData> getOrderedBuckets() {
		List<BucketData> res = new ArrayList<BucketData>(m_bucketSet.values());
		Collections.sort(res, new Comparator<BucketData>() {
			public int compare(BucketData o1, BucketData o2) {
				return o1.getBucketValue().compareTo(o2.getBucketValue());
			}
		});
		return res;
	}

	/**
	 * Fully create the Area thingy.
	 * @see to.etc.domui.component.graph.ICharterHelper#finish()
	 */
	public void finish() throws Exception {
		List<BucketData> list = getOrderedBuckets();

		double[][] data = new double[m_sets.size()][]; // Root thingy.
		for(int i = data.length; --i >= 0;)
			data[i] = new double[list.size()]; // Create value entry for each bucket entry (all default to 0)
		String[] axislabels = new String[list.size()];

		//-- Create all value arrays.
		int bix = 0;
		for(BucketData bd : list) {
			axislabels[bix] = bd.getBucketName();
			for(int i = 0; i < data.length; i++) { // For all datasets in this bucket
				Double v = bd.getValue(i);
				if(v != null)
					data[i][bix] = v.doubleValue();
			}
			bix++;
		}
		DataSeries ds = new DataSeries(axislabels, m_bucketTitle, m_valueTitle, m_title);

		String[] legends = new String[m_sets.size()];
		Paint[] paints = new Paint[m_sets.size()];
		for(int i = m_sets.size(); --i >= 0;) {
			legends[i] = m_sets.get(i).getName();
			paints[i] = m_sets.get(i).getPaint();
		}

		AxisChartDataSet ads = new AxisChartDataSet(data, legends, paints, ChartType.AREA, m_areaProperties);
		ds.addIAxisPlotDataSet(ads);

		AxisChart c = new AxisChart(ds, m_properties, m_axisProperties, m_legendProperties, m_width, m_height);
		m_source.setChart(c);
	}
}
