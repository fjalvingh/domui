package to.etc.domuidemo.pages.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.IPlotlyDataSource;
import to.etc.domui.component.plotly.IPlotlyDataset;
import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.PlotlyGraph;
import to.etc.domui.component.plotly.layout.PlBarMode;
import to.etc.domui.component.plotly.traces.PlLabelValueTrace;
import to.etc.domui.component.plotly.traces.TraceType;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlotlyStackedBar extends UrlPage {
	@Override
	public void createContent() throws Exception {
		PlotlyGraph.initialize(this);
		add(new HTag(1, "A Plotly graph"));

		PlotlyGraph graph = new PlotlyGraph();
		add(graph);
		graph.setHeight("400px");
		graph.setWidth("400px");
		graph.setDisplay(DisplayType.INLINE_BLOCK);
		graph.setSource(new PlotlySource1());
	}

	/**
	 * Sales per employee per year as a stacked bar per employee.
	 */
	static public final class PlotlySource1 implements IPlotlyDataSource {
		@NonNull
		@Override
		public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
			PlotlyDataSet ds = new PlotlyDataSet();
			Map<Employee, List<Invoice>> invPerEmployee = dc.query(QCriteria.create(Invoice.class)).stream()
				.collect(Collectors.groupingBy(a -> a.getCustomer().getSupportRepresentative(), Collectors.toList()))
			;

			Map<Employee, Map<Integer, List<Invoice>>> pymap = new HashMap<>();
			invPerEmployee.forEach((employee, list) -> {
				Map<Integer, List<Invoice>> perYear = list.stream()
					.collect(Collectors.groupingBy(a -> DateUtil.getYear(a.getInvoiceDate()), Collectors.toList()));
				pymap.put(employee, perYear);
			});

			//-- Add all series, per year.
			Map<Integer, PlLabelValueTrace> perYearTraceMap = new HashMap<>();

			pymap.forEach((employee, map) -> {
				map.forEach((year, list) -> {
					PlLabelValueTrace series = perYearTraceMap.computeIfAbsent(year, a -> ds.addLabeledSeries(year.toString()).type(TraceType.Bar)); // Create a series for this year
					series.add(employee == null ? "Unknown" : employee.getLastName(), list.size());
				});
			});
			ds.barMode(PlBarMode.Stack);				// IMPORTANT <-- makes the bars stack
			ds.xAxis().title("Employee");
			ds.title("Sales per year per employee").titleFont().size(25).color("#ff00ff");
			ds.image().bgImage("img/plotly-logo.png", 0.3, 1.0, 0.1);
			return ds;
		}
	}


}
