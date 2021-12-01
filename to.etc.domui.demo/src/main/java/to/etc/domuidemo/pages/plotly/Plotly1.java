package to.etc.domuidemo.pages.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.IPlotlyDataSource;
import to.etc.domui.component.plotly.IPlotlyDataset;
import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.PlotlyGraph;
import to.etc.domui.component.plotly.traces.PlTimeSeriesTrace;
import to.etc.domui.component.plotly.traces.TraceMode;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class Plotly1 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		PlotlyGraph.initialize(this);
		add(new HTag(1, "A Plotly graph"));

		PlotlyGraph graph = new PlotlyGraph();
		add(graph);
		graph.setHeight("1000px");
		graph.setSource(new PlotlySource1());
	}

	static public final class PlotlySource1 implements IPlotlyDataSource {
		@NonNull
		@Override
		public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
			PlotlyDataSet ds = new PlotlyDataSet();
			Map<Date, List<Invoice>> perMonth = dc.query(QCriteria.create(Invoice.class)).stream()
				.collect(Collectors.groupingBy(this::getMonth, Collectors.toList()));
			;

			PlTimeSeriesTrace invoices = ds.addTimeSeries("Invoices").mode(TraceMode.MarkersAndLines);
			perMonth.forEach((date, invoice) -> invoices.add(date, invoices.getSize()));

			//-- Second series of tracks per month
			PlTimeSeriesTrace tracks = ds.addTimeSeries("Tracks");
			perMonth.forEach((date, invoice) -> {
				Integer sum = invoice.stream().map(a -> a.getInvoiceLines().size()).reduce(0, Integer::sum);
				tracks.add(date, sum);
			});

			ds.xAxis().title("Time");
			ds.title("Sales over time").titleFont().size(25).color("#ff00ff");
			ds.image().bgImage("img/plotly-logo.png", 0.3, 1.0, 0.1);
			return ds;
		}

		private Date getMonth(Invoice i) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(i.getInvoiceDate());
			DateUtil.clearTime(cal);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			return cal.getTime();
		}
	}


}
