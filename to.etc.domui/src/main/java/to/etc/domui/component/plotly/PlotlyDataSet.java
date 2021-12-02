package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.layout.PlAnnotation;
import to.etc.domui.component.plotly.layout.PlAxis;
import to.etc.domui.component.plotly.layout.PlBarMode;
import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.component.plotly.layout.PlImage;
import to.etc.domui.component.plotly.traces.IPlotlyTrace;
import to.etc.domui.component.plotly.traces.PlLabelValueTrace;
import to.etc.domui.component.plotly.traces.PlPieTrace;
import to.etc.domui.component.plotly.traces.PlTimeSeriesTrace;
import to.etc.domui.util.javascript.JsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
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

	/**
	 * Add a time series trace, where every pair is a [date, value]. The date
	 * can be either just a date or a timestamp, defined by the timeMode setting
	 * of the trace.
	 */
	public PlTimeSeriesTrace	addTimeSeries(String name) {
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

	@Override
	public void render(@NonNull JsonBuilder b) throws Exception {
		b.obj();
		b.objArrayField("data");
		for(IPlotlyTrace trace : m_traceList) {
			b.itemObj();
			trace.render(b);
			b.objEnd();
		}
		b.arrayEnd();

		b.objObjField("layout");
		b.objFieldOpt("title", m_title);
		PlBarMode barMode = m_barMode;
		if(barMode != null)
			b.objField("barmode", barMode.name().toLowerCase());
		if(! m_titleFont.isEmpty()) {
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
		b.objEnd();						// xaxis

		b.objObjField("yaxis");
		m_yAxis.render(b);
		b.objEnd();						// xaxis

		if(m_annotationList.size() > 0) {
			b.objArrayField("annotations");
			for(PlAnnotation ann : m_annotationList) {
				b.itemObj();
				ann.render(b);
				b.objEnd();
			}
			b.arrayEnd();				// annotations
		}

		b.objEnd();				// layout
		b.objEnd();				// root object
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

	static public void renderColor(JsonBuilder b, String field, String color) throws IOException {
		if(null == color || color.length() == 0)
			return;
		if(! color.startsWith("#"))
			color = "#" + color;
		b.objField(field, color);
	}
}
