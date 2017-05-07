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
import java.awt.font.*;

import org.jCharts.chartData.*;
import org.jCharts.nonAxisChart.*;
import org.jCharts.properties.*;

/**
 * Helper class to initialize a Pie chart.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class PieCharter extends AbstractCharter {

	public class PieCharterProperties {
		private LegendProperties m_legendProperties;

		private ChartProperties m_chartProperties;

		private PieChart2DProperties m_pieChart2DProperties;

		public PieCharterProperties(LegendProperties legendProperties, ChartProperties chartProperties, PieChart2DProperties pieChartProperties) {
			m_legendProperties = legendProperties;
			m_chartProperties = chartProperties;
			m_pieChart2DProperties = pieChartProperties;
		}
	}

	private PieChart2DProperties m_pieChartProperties;

	protected PieCharter(JGraphChartSource source, ChartDimensions chartDimensions) {
		super(source, chartDimensions);
		m_pieChartProperties = new PieChart2DProperties();
		m_pieChartProperties.setBorderPaint(new Color(Integer.parseInt("5C5C5C", 16)));
		m_pieChartProperties.setBorderStroke(new BasicStroke(0.8f));
	}


	protected PieCharter(JGraphChartSource source, ChartDimensions chartDimensions, PieCharterProperties pieCharterProperties) {
		super(source, chartDimensions, pieCharterProperties.m_legendProperties, pieCharterProperties.m_chartProperties);
		m_pieChartProperties = pieCharterProperties.m_pieChart2DProperties;
	}


	/**
	 * Called to actually create the generated thingy.
	 * @see to.etc.domui.component.dynaima.ICharterHelper#finish()
	 */
	@Override
	public void finish() throws Exception {
		PieChartDataSet pds = new PieChartDataSet(null, getChartDataValues(), getChartDataLabels(), selectPaints(), m_pieChartProperties);

		final double fontHeight = getLegendProperties().getFont().getStringBounds(getChartDataLabels()[0], new FontRenderContext(null, false, false)).getHeight();
		final int borders = 15;
		int legendHeight = borders + Math.round((float) (Math.round(((float) getChartDataLabels().length / getLegendProperties().getNumColumns())) * fontHeight));
		int chartHeight = Math.min(m_minheight + legendHeight, m_maxheight);

		final ChartProperties chartProperties = new ChartProperties();

		PieChart2D p2d = new PieChart2D(pds, getLegendProperties(), chartProperties, m_width, chartHeight);

		m_source.setChart(p2d);
	}
}
