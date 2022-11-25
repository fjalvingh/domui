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
	private PlotlyDataSet m_ds = new PlotlyDataSet();

	public GaugeDataSource() {
		m_ds.addTrace(this);
	}

	public GaugeDataSource(double value) {
		this();
		value(value);
	}

	public GaugeDataSource(double value, String text) {
		this();
		value(value);
		title().text(text);
	}

	@NonNull
	@Override
	public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
		mode(PlIndicatorMode.Gauge, PlIndicatorMode.Number);
		return m_ds;
	}

	public PlotlyDataSet dataSet() {
		return m_ds;
	}
}
