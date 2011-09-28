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

	private String m_label;

	private PieChart2DProperties m_pieChartProperties = new PieChart2DProperties();

	protected PieCharter(JGraphChartSource source, String label, int width, int minheight, int maxheight) {
		super(source, label, width, minheight, maxheight);
	}

	public PieChart2DProperties getProperties() {
		return m_pieChartProperties;
	}

	/**
	 * Called to actually create the generated thingy.
	 * @see to.etc.domui.component.dynaima.ICharterHelper#finish()
	 */
	@Override
	public void finish() throws Exception {
		PieChartDataSet pds = new PieChartDataSet(m_label, getChartDataValues(), getChartDataLabels(), selectPaints(), getProperties());
		LegendProperties legendProperties = new LegendProperties();
		legendProperties.setNumColumns(2);

		int legendHeight = (Math.round(pds.getNumberOfDataItems() / 2f)) * 20 + 8;
		int chartHeight = Math.min(m_minheight + legendHeight, m_maxheight);

		PieChart2D p2d = new PieChart2D(pds, legendProperties, new ChartProperties(), m_width, chartHeight);
		m_source.setChart(p2d);
	}
}
