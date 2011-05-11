package to.etc.domui.component.dynaima;

import org.jCharts.axisChart.*;
import org.jCharts.chartData.*;
import org.jCharts.properties.*;
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

	public BarCharter(JGraphChartSource source, String title, int width, int height, String bucketTitle, String valueTitle) {
		super(source, title, width, height);
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
	}

	@Override
	public void finish() throws Exception {
		double[][] data = {getChartDataValues()};
		String[] axislabels = {" "};
		DataSeries ds = new DataSeries(axislabels, m_bucketTitle, m_valueTitle, m_title);
		AxisChartDataSet ads = new AxisChartDataSet(data, getChartDataLabels(), selectPaints(), ChartType.BAR_CLUSTERED, barChartProperties);
		ds.addIAxisPlotDataSet(ads);
		AxisChart c = new AxisChart(ds, m_properties, m_axisProperties, m_legendProperties, m_width, m_height);
		m_source.setChart(c);
	}



}
