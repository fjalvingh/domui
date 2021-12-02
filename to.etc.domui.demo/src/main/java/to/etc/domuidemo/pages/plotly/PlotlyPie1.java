package to.etc.domuidemo.pages.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.IPlotlyDataSource;
import to.etc.domui.component.plotly.IPlotlyDataset;
import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.PlotlyGraph;
import to.etc.domui.component.plotly.traces.PlPieTrace;
import to.etc.domui.derbydata.db.Employee;
import to.etc.domui.derbydata.db.Invoice;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlotlyPie1 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		PlotlyGraph.initialize(this);
		add(new HTag(1, "Plotly - Pie charts"));

		PlotlyGraph graph = new PlotlyGraph();
		add(graph);
		graph.setHeight("400px");
		graph.setWidth("400px");
		graph.setDisplay(DisplayType.INLINE_BLOCK);
		graph.setSource(new PlotlyPieSource());
	}

	/**
	 * Sales per employee.
	 */
	static public final class PlotlyPieSource implements IPlotlyDataSource {
		@NonNull
		@Override
		public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
			PlotlyDataSet ds = new PlotlyDataSet();
			Map<Employee, List<Invoice>> invPerEmployee = dc.query(QCriteria.create(Invoice.class)).stream()
				.collect(Collectors.groupingBy(a -> a.getCustomer().getSupportRepresentative(), Collectors.toList()))
			;
			PlPieTrace pie = ds.addPie();
			invPerEmployee.forEach((employee, list) -> pie.add(employee == null ? "Unknown" : employee.getLastName(), list.size()));

			ds.title("Sales per employee").titleFont().size(25).color("#ff00ff");
			ds.image().bgImage("img/plotly-logo.png", 0.3, 1.0, 0.1);
			return ds;
		}
	}
}
