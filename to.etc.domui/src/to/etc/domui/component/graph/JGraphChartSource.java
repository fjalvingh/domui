package to.etc.domui.component.graph;

import java.awt.image.*;

import org.jCharts.*;

import to.etc.domui.component.dynaima.*;

public class JGraphChartSource implements IBufferedImageSource {
	private Chart				m_chart;
	private ICharterHelper		m_helper;

	public String getMimeType() {
		return "image/png";
	}
	void setChart(Chart c) {
		m_chart = c;
	}

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
		}
		else {
			//---else, create a new BufferedImage and set the Graphics2D onto the chart.
			bufferedImage = new BufferedImage(m_chart.getImageWidth(), m_chart.getImageHeight(), BufferedImage.TYPE_INT_RGB);
			m_chart.setGraphics2D(bufferedImage.createGraphics());
			m_chart.render();
		}
		return bufferedImage;
	}

	public void	createGraph() throws Exception {
	}
	public void	close() {
	}

	public PieCharter		createPieChart(int w, int h, String label) {
		PieCharter	c = new PieCharter(this, label, w, h);
		m_helper = c;
		return c;
	}

	public AreaCharter		createAreaChart(int w, int h, String title, String buckettitle, String valuetitle) {
		AreaCharter	c = new AreaCharter(this, title, w, h, buckettitle, valuetitle);
		m_helper = c;
		return c;
	}
}
