package to.etc.domui.component.plotly.layout;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 04-04-22.
 */
final public class PlMargin {
	private Boolean m_autoExpand;

	private Integer m_bottom;

	private Integer m_left;

	private Integer m_right;

	private Integer m_top;

	private Integer m_pad;

	private PlotlyDataSet m_layout;

	public PlMargin(PlotlyDataSet layout) {
		m_layout = layout;
	}

	public PlotlyDataSet up() {
		return m_layout;
	}

	public Boolean getAutoExpand() {
		return m_autoExpand;
	}

	/**
	 * Turns on/off margin expansion computations. Legends, colorbars, updatemenus,
	 * sliders, axis rangeselector and rangeslider are allowed to push the margins by defaults.
	 */
	public PlMargin autoExpand(Boolean autoExpand) {
		m_autoExpand = autoExpand;
		return this;
	}

	public Integer getBottom() {
		return m_bottom;
	}

	/**
	 * Set the margin as top, right, bottom, left.
	 */
	public PlMargin margins(int top, int right, int bottom, int left) {
		m_top = top;
		m_right = right;
		m_left = left;
		m_bottom = bottom;
		return this;
	}

	/**
	 * Set all margins to the same value.
	 */
	public PlMargin margins(int all) {
		m_right = m_left = m_bottom = m_top = all;
		return this;
	}

	public PlMargin bottom(Integer bottom) {
		m_bottom = bottom;
		return this;
	}

	public Integer getLeft() {
		return m_left;
	}

	public PlMargin left(Integer left) {
		m_left = left;
		return this;
	}

	public Integer getRight() {
		return m_right;
	}

	public PlMargin right(Integer right) {
		m_right = right;
		return this;
	}

	public Integer getTop() {
		return m_top;
	}

	public PlMargin top(Integer top) {
		m_top = top;
		return this;
	}

	public Integer getPad() {
		return m_pad;
	}

	/**
	 * Sets the amount of padding (in px) between the plotting area and the axis lines.
	 */
	public PlMargin pad(Integer pad) {
		m_pad = pad;
		return this;
	}

	public void render(JsonBuilder b) throws IOException {
		b.objField("autoexpand", m_autoExpand);
		b.objField("b", m_bottom);
		b.objField("t", m_top);
		b.objField("l", m_left);
		b.objField("r", m_right);
		b.objField("pad", m_pad);
	}
}
