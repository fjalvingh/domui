package to.etc.domui.component.plotly.layout;

import to.etc.domui.util.DomUtil;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;

/**
 * An image to place somewhere on/under the graph.
 *
 * <p></p>Plotly image placement and sizing is odd!! The x and y position
 * are defined according to the "ref" for either X and/or Y. This ref can be one
 * of X, Y or Paper.</p>
 * <p>If ref is X then the x position is specified in terms of the X axis. So if your x axis is 0..500
 * then 250 would be in the middle. The same goes for ref=Y, then the coordinate is relative to the Y
 * axis. If ref is paper then your X and Y coordinate run from 0..1, where (x, y) = 0,0 means the bottom
 * left corner and (x, y) = (1,1) means top right. As images grow downwards you must specify the TOP
 * coordinate for an image for it to be visible; that top would be (0, 1) normally.
 * </p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-12-21.
 */
public class PlImage {
	private double m_x;

	private double m_y;

	private double m_xSize;

	private double m_ySize;

	private String m_source;

	private PlImageAnchor m_xAnchor;

	private PlImageAnchor m_yAnchor;

	private PlImageRef m_xRef;

	private PlImageRef m_yRef;

	private double m_opacity;

	private PlSizing m_sizing;

	private PlImageLayer m_layer;

	public double getX() {
		return m_x;
	}

	public double getY() {
		return m_y;
	}

	public PlImage pos(double x, double y) {
		m_x = x;
		m_y = y;
		return this;
	}

	public double getXSize() {
		return m_xSize;
	}

	public double getYSize() {
		return m_ySize;
	}

	/**
	 * Define the constants with which to resize the image. A size of 1, 1 means
	 * that the image keeps its original size.
	 */
	public PlImage size(double xSize, double ySize) {
		m_xSize = xSize;
		m_ySize = ySize;
		return this;
	}

	public String getSource() {
		return m_source;
	}

	public PlImage source(String source) {
		m_source = source;
		return this;
	}

	public PlImageAnchor getXAnchor() {
		return m_xAnchor;
	}

	public PlImage xAnchor(PlImageAnchor xAnchor) {
		m_xAnchor = xAnchor;
		return this;
	}

	public PlImageAnchor getYAnchor() {
		return m_yAnchor;
	}

	public PlImage yAnchor(PlImageAnchor yAnchor) {
		m_yAnchor = yAnchor;
		return this;
	}

	public PlImageRef getXRef() {
		return m_xRef;
	}

	public PlImage xRef(PlImageRef xRef) {
		m_xRef = xRef;
		return this;
	}

	public PlImageRef getYRef() {
		return m_yRef;
	}

	public PlImage yRef(PlImageRef yRef) {
		m_yRef = yRef;
		return this;
	}

	public double getOpacity() {
		return m_opacity;
	}

	public PlImage opacity(double opacity) {
		m_opacity = opacity;
		return this;
	}

	public PlSizing getSizing() {
		return m_sizing;
	}

	/**
	 * Setting the sizing to stretch makes the image resize with the size of the
	 * canvas. Sadly enough aspect ratio is not observed so this looks ugly, usually.
	 */
	public PlImage sizing(PlSizing sizing) {
		m_sizing = sizing;
		return this;
	}

	public PlImageLayer getLayer() {
		return m_layer;
	}

	public PlImage layer(PlImageLayer layer) {
		m_layer = layer;
		return this;
	}

	/**
	 *
	 */
	public PlImage bgImage(String source, double opacity) {
		m_source = source;
		m_x = 0;
		m_y = 1;
		//m_sizing = PlSizing.Stretch;
		m_layer = PlImageLayer.Below;
		m_opacity = opacity;
		m_xRef = PlImageRef.Paper;
		m_yRef = PlImageRef.Paper;
		size(1.0, 1.0);
		return this;
	}

	public PlImage bgImage(String source, double x, double y, double opacity) {
		m_source = source;
		m_x = x;
		m_y = y;
		//m_sizing = PlSizing.Stretch;
		m_layer = PlImageLayer.Below;
		m_opacity = opacity;
		m_xRef = PlImageRef.Paper;
		m_yRef = PlImageRef.Paper;
		size(1.0, 1.0);
		return this;
	}


	public void render(JsonBuilder b) throws IOException {
		b.objField("x", m_x);
		b.objField("y", m_y);
		PlImageRef ref = m_xRef;
		if(ref != null)
			b.objField("xref", ref.name().toLowerCase());
		ref = m_yRef;
		if(ref != null)
			b.objField("yref", ref.name().toLowerCase());
		if(m_xSize > 0)
			b.objField("sizex", m_xSize);
		if(m_ySize > 0)
			b.objField("sizey", m_ySize);
		PlImageAnchor a = m_xAnchor;
		if(null != a)
			b.objField("xanchor", a.name().toLowerCase());
		a = m_yAnchor;
		if(null != a)
			b.objField("yanchor", a.name().toLowerCase());
		b.objFieldOpt("source", DomUtil.calculateImageURL(m_source, false));
		PlSizing s = m_sizing;
		if(null != s) {
			b.objField("sizing", s.name().toLowerCase());
		}
		if(m_opacity > 0)
			b.objField("opacity", m_opacity);
		PlImageLayer l = m_layer;
		if(null != l)
			b.objField("layer", l.name().toLowerCase());
	}
}
