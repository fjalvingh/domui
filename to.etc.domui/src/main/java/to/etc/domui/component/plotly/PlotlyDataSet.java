package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.traces.IPlotlyTrace;
import to.etc.domui.component.plotly.traces.PlTimeSeriesTrace;
import to.etc.domui.util.javascript.JsonBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlotlyDataSet implements IPlotlyDataset {
	private List<IPlotlyTrace> m_traceList = new ArrayList<>();

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

		b.objEnd();
		b.objEnd();
	}
}
