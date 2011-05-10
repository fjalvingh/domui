package to.etc.domui.component.dynaima;

import java.awt.*;
import java.util.*;
import java.util.List;

import org.jCharts.axisChart.*;
import org.jCharts.chartData.*;
import org.jCharts.properties.*;
import org.jCharts.types.*;

public class BarCharter implements ICharterHelper {

	private JGraphChartSource m_source;

	ClusteredBarChartProperties barChartProperties = new ClusteredBarChartProperties();

	private ChartProperties m_properties = new ChartProperties();

	private AxisProperties m_axisProperties = new AxisProperties();

	private LegendProperties m_legendProperties = new LegendProperties();

	private int m_width = 600;

	private int m_height = 500;

	private String m_bucketTitle;

	private String m_valueTitle;

	private String m_title;

	private List<ChartDataElement> chartDataElements;

	Paint[] availablePaints = new Paint[]{Color.yellow, Color.red, Color.green, Color.blue, Color.PINK};



	public BarCharter(JGraphChartSource source, String title, int width, int height, String bucketTitle, String valueTitle) {
		super();
		m_source = source;
		m_width = width;
		m_height = height;
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
		m_title = title;
		chartDataElements = new ArrayList<ChartDataElement>();
	}

	public void addChartDataElemet(ChartDataElement element) {
		chartDataElements.add(element);
	}

	@Override
	public void finish() throws Exception {
		double[][] data = new double[chartDataElements.size()][1];

		String[] legendLabels = new String[chartDataElements.size()];

		for  (int i=0;i<chartDataElements.size();i++) {
			data[i][0] = chartDataElements.get(i).getValue();
		}

		for(int i = 0; i < chartDataElements.size(); i++) {
			legendLabels[i] = chartDataElements.get(i).getLabel();
		}

		String[] axislabels = {" "};
		DataSeries ds = new DataSeries(axislabels, m_bucketTitle, m_valueTitle, m_title);
		AxisChartDataSet ads = new AxisChartDataSet(data, legendLabels, selectPaints(chartDataElements.size()), ChartType.BAR_CLUSTERED, barChartProperties);
		ds.addIAxisPlotDataSet(ads);

		AxisChart c = new AxisChart(ds, m_properties, m_axisProperties, m_legendProperties, m_width, m_height);
		m_source.setChart(c);
	}

	private Paint[] selectPaints(int size) {
		Paint[] resultSet = new Paint[size];
		for(int i = 0; i < size; i++) {
			resultSet[i] = availablePaints[new Random().nextInt(5)];
		}
		return resultSet;
	}



}
