package to.etc.domui.component.dynaima;

import java.awt.*;
import java.awt.font.*;

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

	private ClusteredBarChartProperties m_barChartProperties;

	private String m_bucketTitle;

	private String m_valueTitle;

	public class BarCharterParameters {
		private LegendProperties m_isLegendProperties;

		private ChartProperties m_isChartProperties;

		private ClusteredBarChartProperties m_isBarChartProperties;

		public BarCharterParameters(LegendProperties legendProperties, ChartProperties chartProperties, ClusteredBarChartProperties barChartProperties) {
			m_isLegendProperties = legendProperties;
			m_isChartProperties = chartProperties;
			m_isBarChartProperties = barChartProperties;
		}
	}

	public BarCharter(JGraphChartSource source, ChartDimensions chartDimensions, String bucketTitle, String valueTitle) {
		super(source, chartDimensions);
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
		m_barChartProperties = new ClusteredBarChartProperties();
		m_barChartProperties.setBarOutlineStroke(new ChartStroke(new BasicStroke(0.8f), new Color(Integer.parseInt("5C5C5C", 16))));
	}

	public BarCharter(JGraphChartSource source, BarCharterParameters barCharterParameters, ChartDimensions chartDimensions, String bucketTitle, String valueTitle) {
		super(source, chartDimensions, barCharterParameters.m_isLegendProperties, barCharterParameters.m_isChartProperties);
		m_barChartProperties = barCharterParameters.m_isBarChartProperties;
		m_bucketTitle = bucketTitle;
		m_valueTitle = valueTitle;
	}


	@Override
	public void finish() throws Exception {
		double[][] data = getChartDataAsMatrix();
		String[] axisLabels = {" "};
		DataSeries ds = new DataSeries(axisLabels, m_bucketTitle, m_valueTitle, null);

		String[] cdl = getChartDataLabels();
		final double fontHeight = getLegendProperties().getFont().getStringBounds(cdl[0], new FontRenderContext(null, false, false)).getHeight();
		final int borders = 15;
		int legendHeight = borders + Math.round((float) (Math.round(((float) cdl.length / getLegendProperties().getNumColumns())) * fontHeight));
		int chartHeight = Math.min(m_minheight + legendHeight, m_maxheight);
		
		AxisChartDataSet ads = new AxisChartDataSet(data, cdl, selectPaints(), ChartType.BAR_CLUSTERED, m_barChartProperties);
		ds.addIAxisPlotDataSet(ads);

		AxisChart c = new AxisChart(ds, getProperties(), m_axisProperties, getLegendProperties(), m_width, chartHeight);
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
