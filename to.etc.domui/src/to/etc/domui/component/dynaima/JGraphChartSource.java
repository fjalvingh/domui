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

import java.awt.image.*;

import org.jCharts.*;

import to.etc.domui.component.dynaima.*;

public class JGraphChartSource implements IBufferedImageSource {
	private Chart m_chart;

	private ICharterHelper m_helper;

	@Override
	public String getMimeType() {
		return "image/png";
	}

	void setChart(Chart c) {
		m_chart = c;
	}

	@Override
	public BufferedImage getImage() throws Exception {
		try {
			createGraph();
		} finally {
			close();
		}

		if(m_helper != null) {
			m_helper.finish();
			m_helper = null;
		}
		System.out.println("DYNAIMA: Generating jGraph graph");
		BufferedImage bufferedImage = null;

		//---if we use an ImageMap, we already have rendered the chart by the time we get here so,
		//---   simply return the rendered image.
		if(m_chart.getGenerateImageMapFlag()) {
			bufferedImage = m_chart.getBufferedImage();
		} else {
			//---else, create a new BufferedImage and set the Graphics2D onto the chart.
			bufferedImage = new BufferedImage(m_chart.getImageWidth(), m_chart.getImageHeight(), BufferedImage.TYPE_INT_RGB);
			m_chart.setGraphics2D(bufferedImage.createGraphics());
			m_chart.render();
		}
		return bufferedImage;
	}

	public void createGraph() throws Exception {}

	public void close() {}

	public PieCharter createPieChart(int w, int h, String label) {
		PieCharter c = new PieCharter(this, label, w, h);
		m_helper = c;
		return c;
	}

	public AreaCharter createAreaChart(int w, int h, String title, String buckettitle, String valuetitle) {
		AreaCharter c = new AreaCharter(this, title, w, h, buckettitle, valuetitle);
		m_helper = c;
		return c;
	}
}
