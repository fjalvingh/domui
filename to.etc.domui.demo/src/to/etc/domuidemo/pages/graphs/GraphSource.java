package to.etc.domuidemo.pages.graphs;

import java.awt.image.*;

import org.jCharts.*;

import to.etc.domui.component.dynaima.*;

/**
 * A JGraph-based chart source.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class GraphSource implements IBufferedImageSource {
	private Chart				m_chart;

	protected GraphSource(Chart chart) {
		m_chart = chart;
	}

	@Override
	public String getMimeType() {
		return "image/png";
	}

	@Override
	public BufferedImage getImage() throws Exception {
		System.out.println("DYNAIMA: Generating jGraph graph");
		BufferedImage bufferedImage = null;

		//---if we use an ImageMap, we already have rendered the chart byt the time we get here so,
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
}
