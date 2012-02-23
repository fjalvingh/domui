package to.etc.domui.component.dynaima;

/**
 * Used to define the size of the Chart. 
 *
 * @author <a href="mailto:nmaksimovic@execom.eu">Nemanja Maksimovic</a>
 * Created on 29 Sep 2011
 */
public class ChartDimensions {

	private final int m_width;

	private final int m_minheight;

	private final int m_maxheight;

	/**
	 * @param width of the chart.
	 * @param minheight height without any legend.
	 * @param maxheight maximum height of chart + legend.
	 */
	public ChartDimensions(int width, int minheight, int maxheight) {
		m_width = width;
		m_minheight = minheight;
		m_maxheight = maxheight;
	}

	public int getWidth() {
		return m_width;
	}

	public int getMinheight() {
		return m_minheight;
	}

	public int getMaxheight() {
		return m_maxheight;
	}
}