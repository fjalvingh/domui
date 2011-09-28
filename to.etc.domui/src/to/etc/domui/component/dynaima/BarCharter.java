package to.etc.domui.component.dynaima;

import java.awt.*;

import org.jCharts.axisChart.*;
import org.jCharts.chartData.*;
import org.jCharts.properties.*;
import org.jCharts.properties.util.*;
import org.jCharts.types.*;

/**
 * Helper class to initialize a Bar chart.
 *
 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
 * Created on 11 May 2011
 */
public class BarCharter extends AbstractCharter {

	private ClusteredBarChartProperties barChartProperties = new ClusteredBarChartProperties();

	private String m_bucketTitle;

	private String m_valueTitle;

	public BarCharter(JGraphChartSource source, String title, int width, int minheight, int maxheight, String bucketTitle, String valueTitle) {
		super(source, title, width, minheight, maxheight);
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
	}

	@Override
	public void finish() throws Exception {
		double[][] data = getChartDataAsMatrix();
		String[] axisLabels = {" "};
		DataSeries ds = new DataSeries(axisLabels, m_bucketTitle, m_valueTitle, null);

		String[] cdl = getChartDataLabels();
		int legendHeight = (Math.round(cdl.length / 2f)) * 20 + 8;
		int chartHeight = Math.min(m_minheight + legendHeight, m_maxheight);
		
		barChartProperties.setBarOutlineStroke(new ChartStroke(new BasicStroke(0.8f), new Color(Integer.parseInt("5C5C5C", 16))));

		AxisChartDataSet ads = new AxisChartDataSet(data, cdl, selectPaints(), ChartType.BAR_CLUSTERED, barChartProperties);
		ds.addIAxisPlotDataSet(ads);

		AxisChart c = new AxisChart(ds, m_properties, m_axisProperties, getLegendProperties(), m_width, chartHeight);
		m_source.setChart(c);
	}

	private double[][] getChartDataAsMatrix() {
		double[] original = getChartDataValues();
		double[][] result = new double[original.length][1];
		for(int i = 0; i < original.length; i++) {
			result[i][0] = original[i];
		}
		return result;
	}



}
