package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.IPlotlyDataSource;
import to.etc.domui.component.plotly.IPlotlyDataset;
import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class GaugeDataSource extends PlGaugeTrace implements IPlotlyDataSource {
	public GaugeDataSource() {
	}

	public GaugeDataSource(double value) {
		value(value);
	}

	public GaugeDataSource(double value, String text) {
		value(value);
		title().text(text);
	}

	@NonNull
	@Override
	public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
		PlotlyDataSet ds = new PlotlyDataSet();
		ds.addTrace(this);
		mode(PlIndicatorMode.Gauge, PlIndicatorMode.Number);
		return ds;
	}
}
