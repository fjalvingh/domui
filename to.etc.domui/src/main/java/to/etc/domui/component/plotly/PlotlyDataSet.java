package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.layout.PlAxis;
import to.etc.domui.component.plotly.layout.PlFont;
import to.etc.domui.component.plotly.traces.IPlotlyTrace;
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

	private PlAxis m_xAxis = new PlAxis();

	private PlAxis m_yAxis = new PlAxis();

	private String m_title;

	private PlFont m_titleFont = new PlFont();

	public PlTimeSeriesTrace	addTimeSeries(String name) {
		PlTimeSeriesTrace tst = new PlTimeSeriesTrace().name(name);
		m_traceList.add(tst);
		return tst;
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
		if(! m_titleFont.isEmpty()) {
			b.objObjField("titlefont");
			m_titleFont.render(b);
			b.objEnd();
		}

		b.objObjField("xaxis");
		m_xAxis.render(b);
		b.objEnd();						// xaxis

		b.objObjField("yaxis");
		m_yAxis.render(b);
		b.objEnd();						// xaxis

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

	static public void renderColor(JsonBuilder b, String field, String color) throws IOException {
		if(null == color || color.length() == 0)
			return;
		if(! color.startsWith("#"))
			color = "#" + color;
		b.objField(field, color);
	}
}
