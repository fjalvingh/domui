/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.dynaima;

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

	private int m_width, m_minheight, m_maxheight;

	private String m_valueTitle;

	private AreaChartProperties m_areaProperties = new AreaChartProperties();

	private ChartProperties m_properties = new ChartProperties();

	private AxisProperties m_axisProperties = new AxisProperties();

	private LegendProperties m_legendProperties = new LegendProperties();

	// FIXME Cannot create ANY construct here that does NOT cause generic warnings. Good job, Sun.
	@SuppressWarnings({"unchecked"})
	static public class BucketData {
		private String m_bucketName;

		private Object m_bucketValue;

		private List<Double> m_values = new ArrayList<Double>();

		protected BucketData(String bucketName, Object bucketValue) {
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

		public Object getBucketValue() {
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

		public <T extends Comparable<T>> void add(String bucketlabel, T sortvalue, double value) {
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

	protected AreaCharter(JGraphChartSource source, String title, ChartDimensions chartDimensions, String bucketTitle, String valueTitle) {
		m_source = source;
		m_title = title;
		m_width = chartDimensions.getWidth();
		m_minheight = chartDimensions.getMinheight();
		m_maxheight = chartDimensions.getMaxheight();
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
	}

	public DataSet addDataSet(Paint paint, String name) {
		DataSet ds = new DataSet(paint, name, m_sets.size());
		m_sets.add(ds);
		return ds;
	}

	<T extends Comparable<T>> void _add(int index, String bucketlabel, T sortvalue, double value) {
		BucketData bd = addBucket(bucketlabel, sortvalue); // Add/get bucket.
		bd.setValue(index, value);
	}

	public BucketData addBucket(String bucketlabel, Object sortvalue) {
		BucketData d = m_bucketSet.get(sortvalue);
		if(d == null) {
			d = new BucketData(bucketlabel, sortvalue);
			m_bucketSet.put(sortvalue, d);
		}
		return d;
	}

	public List<BucketData> getOrderedBuckets() {
		Collection<?> values = m_bucketSet.values();
		List<BucketData> res = new ArrayList<BucketData>((Collection<BucketData>) values);
		Collections.sort(res, new Comparator<BucketData>() {
			@Override
			public int compare(BucketData o1, BucketData o2) {
				Comparable<Object> a = (Comparable<Object>) o1.getBucketValue();
				Comparable<Object> b = (Comparable<Object>) o2.getBucketValue();
				return a.compareTo(b);
			}
		});
		return res;
	}

	/**
	 * Fully create the Area thingy.
	 * @see to.etc.domui.component.dynaima.ICharterHelper#finish()
	 */
	@Override
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

		int legendHeight = (Math.round(legends.length / 2f)) * 20 + 8;
		int chartHeight = Math.min(m_minheight + legendHeight, m_maxheight);

		AxisChartDataSet ads = new AxisChartDataSet(data, legends, paints, ChartType.AREA, m_areaProperties);
		ds.addIAxisPlotDataSet(ads);

		AxisChart c = new AxisChart(ds, m_properties, m_axisProperties, m_legendProperties, m_width, chartHeight);
		m_source.setChart(c);
	}

	@Override
	public void addChartField(ChartField element) {
		// TODO(nmaksimovic) Integrate with the rest of the charts.
	}
}
