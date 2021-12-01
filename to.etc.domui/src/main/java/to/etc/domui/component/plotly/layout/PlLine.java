package to.etc.domui.component.plotly.layout;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * Line class, as part of a Trace - <a href="https://plotly.com/python/reference/scatter/#scatter-line>See here</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-12-21.
 */
public class PlLine {
	private String m_color;

	private PlDash m_dash;

	private PlShape m_shape;

	private boolean m_simplify;

	private double m_smoothing;

	private double m_width;

	public String getColor() {
		return m_color;
	}

	/**
	 * Sets the line color.
	 */
	public PlLine color(String color) {
		m_color = color;
		return this;
	}

	public PlDash getDash() {
		return m_dash;
	}

	/**
	 * Sets the dash style of lines. Set to a dash type string ("solid", "dot", "dash", "longdash", "dashdot", or "longdashdot").
	 */
	public PlLine dash(PlDash dash) {
		m_dash = dash;
		return this;
	}

	public PlShape getShape() {
		return m_shape;
	}

	/**
	 * Determines the line shape. With "spline" the lines are drawn using spline interpolation. The other
	 * available values correspond to step-wise line shapes.
	 */
	public PlLine shape(PlShape shape) {
		m_shape = shape;
		return this;
	}

	public boolean isSimplify() {
		return m_simplify;
	}

	/**
	 * Simplifies lines by removing nearly-collinear points. When transitioning lines, it
	 * may be desirable to disable this so that the number of points along the resulting
	 * SVG path is unaffected.
	 */
	public PlLine simplify() {
		m_simplify = true;
		return this;
	}

	public double getSmoothing() {
		return m_smoothing;
	}

	/**
	 * Has an effect only if `shape` is set to "spline" Sets the amount of smoothing.
	 * "0" corresponds to no smoothing (equivalent to a "linear" shape).
	 */
	public PlLine smoothing(double smoothing) {
		m_smoothing = smoothing;
		return this;
	}

	public double getWidth() {
		return m_width;
	}

	/**
	 * Sets the line width (in px).
	 */
	public PlLine width(double width) {
		m_width = width;
		return this;
	}

	public void render(JsonBuilder b) throws IOException {
		PlotlyDataSet.renderColor(b, "color", m_color);
		if(m_simplify)
			b.objField("simplify", true);
		PlDash d = m_dash;
		if(null != d)
			b.objField("dash", d.name().toLowerCase());
		PlShape s = m_shape;
		if(null != s)
			b.objField("shape", s.name().toLowerCase());
		if(m_smoothing > 0.0)
			b.objField("smoothing", m_smoothing);
		if(m_width > 0)
			b.objField("width", m_width);
	}

	public boolean isEmpty() {
		return m_color == null
			&& ! m_simplify
			&& m_dash == null
			&& m_shape == null
			&& m_smoothing <= 0.0
			&& m_width == 0
			;
	}

}
