package to.etc.domui.component.graph;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jCharts.chartData.*;
import org.jCharts.nonAxisChart.*;
import org.jCharts.properties.*;

/**
 * Helper class to initialize a Pie chart.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class PieCharter implements ICharterHelper {
	private JGraphChartSource m_source;

	private String m_label;

	private int m_width, m_height;

	private List<Paint> m_paintList = new ArrayList<Paint>();

	private List<Double> m_valueList = new ArrayList<Double>();

	private List<String> m_labelList = new ArrayList<String>();

	private PieChart2DProperties m_properties = new PieChart2DProperties();

	protected PieCharter(JGraphChartSource source, String label, int width, int height) {
		m_source = source;
		m_label = label;
		m_width = width;
		m_height = height;
	}

	public PieChart2DProperties getProperties() {
		return m_properties;
	}

	public void addPoint(Paint pnt, String label, double value) {
		m_paintList.add(pnt);
		m_valueList.add(Double.valueOf(value));
		m_labelList.add(label);
	}

	/**
	 * Called to actually create the generated thingy.
	 * @see to.etc.domui.component.graph.ICharterHelper#finish()
	 */
	@Override
	public void finish() throws Exception {
		double[] res = new double[m_valueList.size()];
		for(int i = res.length; --i >= 0;)
			res[i] = m_valueList.get(i).doubleValue();

		PieChartDataSet pds = new PieChartDataSet(m_label, res, m_labelList.toArray(new String[m_labelList.size()]), m_paintList.toArray(new Paint[m_paintList.size()]), getProperties());
		PieChart2D p2d = new PieChart2D(pds, new LegendProperties(), new ChartProperties(), m_width, m_height);
		m_source.setChart(p2d);
	}
}
