package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.plotly.layout.PlAnnotation;
import to.etc.domui.component.plotly.layout.PlAxis;
import to.etc.domui.component.plotly.layout.PlBarMode;
import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.component.plotly.layout.PlImage;
import to.etc.domui.component.plotly.traces.IPlotlyTrace;
import to.etc.domui.component.plotly.traces.PlLabelValueTrace;
import to.etc.domui.component.plotly.traces.PlPieTrace;
import to.etc.domui.component.plotly.traces.PlSunBurstTrace;
import to.etc.domui.component.plotly.traces.PlTimeSeriesTrace;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlotlyDataSet implements IPlotlyDataset {
	private List<IPlotlyTrace> m_traceList = new ArrayList<>();

	/*----------------------------------------------------------------------*/
	/*	CODING:	Layout object properties.									*/
	/*----------------------------------------------------------------------*/

	private PlAxis m_xAxis = new PlAxis();

	private PlAxis m_yAxis = new PlAxis();

	private String m_title;

	private PlFont m_titleFont = new PlFont();

	private Boolean m_showLegend;

	private boolean m_legendHorizontal;

	private PlBarMode m_barMode;

	final private List<PlImage> m_imageList = new ArrayList<>(4);

	final private List<PlAnnotation> m_annotationList = new ArrayList<>(4);

	final private List<String> m_colorWay = new ArrayList<>();

	final private List<String> m_sunburstColorWay = new ArrayList<>();

	private boolean m_extendSunburstColorway;

	private int m_gridRows;

	private int m_gridColumns;

	private int m_width;

	private int m_height;

	/**
	 * Add a time series trace, where every pair is a [date, value]. The date
	 * can be either just a date or a timestamp, defined by the timeMode setting
	 * of the trace.
	 */
	public PlTimeSeriesTrace addTimeSeries(String name) {
		PlTimeSeriesTrace tst = new PlTimeSeriesTrace().name(name);
		m_traceList.add(tst);
		return tst;
	}

	/**
	 * Add a trace containing [String, value] pairs, used for bar charts or scatter
	 * plots (no time series).
	 */
	public PlLabelValueTrace addLabeledSeries(String name) {
		PlLabelValueTrace t = new PlLabelValueTrace().name(name);
		m_traceList.add(t);
		return t;
	}

	/**
	 * Add a pie dataset, using [string, value] pairs of data.
	 */
	public PlPieTrace addPie() {
		PlPieTrace t = new PlPieTrace();
		m_traceList.add(t);
		return t;
	}

	public PlSunBurstTrace addSunburst() {
		PlSunBurstTrace t = new PlSunBurstTrace();
		m_traceList.add(t);
		return t;
	}

	@Override
	public void render(@NonNull JsonBuilder b) throws Exception {
		b.obj();
		b.objArrayField("data");
		for(IPlotlyTrace trace : m_traceList) {
			b.itemObj();
			trace.render(b);
			b.objEnd();

			if(trace instanceof PlPieTrace) {
				PlPieTrace pt = (PlPieTrace) trace;
				if(pt.getDomainX() > 0 && pt.getDomainX() >= m_gridColumns) {
					m_gridColumns = pt.getDomainX() + 1;
				}
				if(pt.getDomainY() > 0 && pt.getDomainY() >= m_gridRows) {
					m_gridRows = pt.getDomainY() + 1;
				}
			}
		}
		b.arrayEnd();

		b.objObjField("layout");

		if(m_width > 0)
			b.objField("width", m_width);
		if(m_height > 0)
			b.objField("height", m_height);

		b.objFieldOpt("title", m_title);
		PlBarMode barMode = m_barMode;
		if(barMode != null)
			b.objField("barmode", barMode.name().toLowerCase());
		if(!m_titleFont.isEmpty()) {
			b.objObjField("titlefont");
			m_titleFont.render(b);
			b.objEnd();
		}
		b.objField("showlegend", m_showLegend);
		if(m_legendHorizontal) {
			b.objObjField("legend");
			b.objField("orientation", "h");
			b.objEnd();
		}

		if(m_imageList.size() > 0) {
			b.objArrayField("images");
			for(PlImage image : m_imageList) {
				b.itemObj();
				image.render(b);
				b.objEnd();
			}
			b.arrayEnd();
		}

		b.objObjField("xaxis");
		m_xAxis.render(b);
		b.objEnd();                        // xaxis

		b.objObjField("yaxis");
		m_yAxis.render(b);
		b.objEnd();                        // xaxis

		if(m_annotationList.size() > 0) {
			b.objArrayField("annotations");
			for(PlAnnotation ann : m_annotationList) {
				b.itemObj();
				ann.render(b);
				b.objEnd();
			}
			b.arrayEnd();                // annotations
		}

		if(m_gridRows > 0 || m_gridColumns > 0) {
			if(m_gridRows == 0)
				m_gridRows = 1;
			if(m_gridColumns == 0)
				m_gridColumns = 1;
			b.objObjField("grid");
			b.objField("rows", m_gridRows);
			b.objField("columns", m_gridColumns);
			b.objEnd();
		}

		if(m_colorWay.size() > 0) {
			b.objArrayField("colorway");
			for(String s : m_colorWay) {
				b.item(fixColor(s));
			}
			b.arrayEnd();
		}
		if(m_sunburstColorWay.size() > 0) {
			b.objArrayField("sunburstcolorway");
			for(String s : m_sunburstColorWay) {
				b.item(fixColor(s));
			}
			b.arrayEnd();
		}
		if(m_extendSunburstColorway)
			b.objField("extendsunburstcolorway", true);


		//b.objField("extendsunburstcolorway", true);
		//b.objArrayField("sunburstcolorway");
		//for(String s : Arrays.asList("#636efa", "#EF553B", "#00cc96", "#ab63fa", "#19d3f3",
		//	"#e763fa", "#FECB52", "#FFA15A", "#FF6692", "#B6E880")) {
		//	b.item(s);
		//}
		//b.arrayEnd();

		b.objEnd();                // layout
		b.objEnd();                // root object
	}

	public PlAxis xAxis() {
		return m_xAxis;
	}

	public PlAxis yAxis() {
		return m_yAxis;
	}

	public PlotlyDataSet title(String title) {
		m_title = title;
		return this;
	}

	public PlFont titleFont() {
		return m_titleFont;
	}

	public PlotlyDataSet showLegend(boolean on) {
		m_showLegend = on;
		return this;
	}

	public PlotlyDataSet width(int w) {
		m_width = w;
		return this;
	}

	public PlotlyDataSet height(int w) {
		m_height = w;
		return this;
	}

	public PlotlyDataSet size(int w, int h) {
		m_width = w;
		m_height = h;
		return this;
	}

	public PlotlyDataSet legendHorizontal() {
		m_legendHorizontal = true;
		return this;
	}

	public PlImage image() {
		PlImage image = new PlImage();
		m_imageList.add(image);
		return image;
	}

	public PlotlyDataSet barMode(PlBarMode mode) {
		m_barMode = mode;
		return this;
	}

	/**
	 * Add an annotation and return the annotation object to be further configured.
	 */
	public PlAnnotation addAnnotation(double x, double y, String text) {
		PlAnnotation ann = new PlAnnotation(x, y, text);
		m_annotationList.add(ann);
		return ann;
	}

	public PlotlyDataSet annotation(double x, double y, String text) {
		addAnnotation(x, y, text);
		return this;
	}

	public PlotlyDataSet grid(int columns, int rows) {
		m_gridColumns = columns;
		m_gridRows = rows;
		return this;
	}

	static public void renderColor(JsonBuilder b, String field, String color) throws IOException {
		color = fixColor(color);
		if(color == null)
			return;
		b.objField(field, color);
	}

	@Nullable
	private static String fixColor(String color) {
		if(null == color || color.length() == 0)
			return null;
		for(int i = color.length(); --i >= 0;) {
			char c = color.charAt(i);
			if(! isDigit(c))
				return color;
		}
		if(!color.startsWith("#"))
			color = "#" + color;
		return color;
	}

	static private boolean isDigit(char c) {
		if(c >= '0' && c <= '9')
			return true;
		if(c >= 'A' && c <= 'F')
			return true;
		return c >= 'a' && c <= 'f';
	}

	public PlotlyDataSet colorWay(List<String> colors) {
		m_colorWay.addAll(colors);
		return this;
	}

	public PlotlyDataSet colorWay(String... colors) {
		colorWay(Arrays.asList(colors));
		return this;
	}

	public PlotlyDataSet sunburstColorWay(List<String> colors) {
		m_sunburstColorWay.addAll(colors);
		return this;
	}

	public PlotlyDataSet sunburstColorWay(String... colors) {
		sunburstColorWay(Arrays.asList(colors));
		return this;
	}

	public PlotlyDataSet extendSunburstColorway() {
		m_extendSunburstColorway = true;
		return this;
	}

	public void addTrace(IPlotlyTrace trace) {
		m_traceList.add(trace);

	}
}
