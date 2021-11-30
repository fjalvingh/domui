package to.etc.domui.component.plotly.layout;

import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * A plotly axis.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlAxis {
	private Boolean m_autoTick;

	private AxisTick m_axisTick;

	private Object m_tickZero;

	private Object m_dtick;

	private int m_tickLen;

	private int m_tickWidth;

	private String m_tickColor;

	private Boolean m_showGrid;

	private Boolean m_zeroLine;

	private Boolean m_showLine;

	private String m_gridColor;

	private int m_gridWidth;

	private String m_zeroLineColor;

	private int m_zeroLineWidth;

	private String m_lineColor;

	private int m_lineWidth;

	private boolean m_logarithmic;

	private String m_title;

	private PlFont m_titleFont = new PlFont();

	public Boolean getAutoTick() {
		return m_autoTick;
	}

	public PlAxis autoTick(boolean autoTick) {
		m_autoTick = autoTick;
		return this;
	}

	public AxisTick getAxisTick() {
		return m_axisTick;
	}

	public PlAxis axisTick(AxisTick axisTick) {
		m_axisTick = axisTick;
		return this;
	}

	public Object getTickZero() {
		return m_tickZero;
	}

	public PlAxis tickZero(Object tickZero) {
		m_tickZero = tickZero;
		return this;
	}

	public Object getDtick() {
		return m_dtick;
	}

	public PlAxis dtick(Object dtick) {
		m_dtick = dtick;
		return this;
	}

	public int getTickLen() {
		return m_tickLen;
	}

	public PlAxis tickLen(int tickLen) {
		m_tickLen = tickLen;
		return this;
	}

	public int getTickWidth() {
		return m_tickWidth;
	}

	public PlAxis tickWidth(int tickWidth) {
		m_tickWidth = tickWidth;
		return this;
	}

	public String getTickColor() {
		return m_tickColor;
	}

	public PlAxis tickColor(String tickColor) {
		m_tickColor = tickColor;
		return this;
	}

	public Boolean getShowGrid() {
		return m_showGrid;
	}

	public PlAxis showGrid(Boolean showGrid) {
		m_showGrid = showGrid;
		return this;
	}

	public Boolean getZeroLine() {
		return m_zeroLine;
	}

	public PlAxis zeroLine(Boolean zeroLine) {
		m_zeroLine = zeroLine;
		return this;
	}

	public Boolean getShowLine() {
		return m_showLine;
	}

	public PlAxis showLine(Boolean showLine) {
		m_showLine = showLine;
		return this;
	}

	public String getGridColor() {
		return m_gridColor;
	}

	public PlAxis gridColor(String gridColor) {
		m_gridColor = gridColor;
		return this;
	}

	public int getGridWidth() {
		return m_gridWidth;
	}

	public PlAxis gridWidth(int gridWidth) {
		m_gridWidth = gridWidth;
		return this;
	}

	public String getZeroLineColor() {
		return m_zeroLineColor;
	}

	public PlAxis zeroLineColor(String zeroLineColor) {
		m_zeroLineColor = zeroLineColor;
		return this;
	}

	public int getZeroLineWidth() {
		return m_zeroLineWidth;
	}

	public PlAxis zeroLineWidth(int zeroLineWidth) {
		m_zeroLineWidth = zeroLineWidth;
		return this;
	}

	public String getLineColor() {
		return m_lineColor;
	}

	public PlAxis lineColor(String lineColor) {
		m_lineColor = lineColor;
		return this;
	}

	public int getLineWidth() {
		return m_lineWidth;
	}

	public PlAxis lineWidth(int lineWidth) {
		m_lineWidth = lineWidth;
		return this;
	}

	public PlAxis logarithmic() {
		m_logarithmic = true;
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public PlAxis title(String title) {
		m_title = title;
		return this;
	}

	public PlFont titleFont() {
		return m_titleFont;
	}

	/**
	 * Render object content.
	 */
	public void render(JsonBuilder b) throws Exception {
		if(m_logarithmic) {
			b.objField("type", "log");
		}
		Boolean bv = m_autoTick;
		if(null != bv) {
			b.objField("autotick", bv);
		}
		AxisTick axisTick = m_axisTick;
		if(null != axisTick) {
			b.objField("ticks", axisTick.name().toLowerCase());
		}
		Object tickZero = m_tickZero;

		int v = m_tickLen;
		if(v > 0) {
			b.objField("ticklen", v);
		}
		v = m_tickWidth;
		if(v > 0) {
			b.objField("tickwidth", v);
		}
		String s = m_tickColor;
		if(s != null && s.length() > 0) {
			if(! s.startsWith("#"))
				s = "#" + s;
			b.objField("tickcolor", s);
		}
		b.objField("showgrid", m_showGrid);
		b.objField("zeroline", m_zeroLine);
		b.objField("showline", m_showLine);
		PlotlyDataSet.renderColor(b, "gridcolor", m_gridColor);
		if(m_gridWidth > 0)
			b.objField("gridwidth", m_gridWidth);
		PlotlyDataSet.renderColor(b, "zerolinecolor", m_zeroLineColor);
		if(m_zeroLineWidth > 0)
			b.objField("zerolinewidth", m_zeroLineWidth);
		PlotlyDataSet.renderColor(b, "linecolor", m_lineColor);
		if(m_lineWidth > 0)
			b.objField("linewidth", m_lineWidth);

		String title = m_title;
		if(null != title) {
			b.objField("title", title);
			if(!m_titleFont.isEmpty()) {
				b.objObjField("titlefont");
				m_titleFont.render(b);
				b.objEnd();
			}
		}
	}
}
