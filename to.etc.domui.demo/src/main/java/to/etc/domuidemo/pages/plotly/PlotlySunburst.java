package to.etc.domuidemo.pages.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.plotly.IPlotlyDataSource;
import to.etc.domui.component.plotly.IPlotlyDataset;
import to.etc.domui.component.plotly.PlotlyDataSet;
import to.etc.domui.component.plotly.PlotlyGraph;
import to.etc.domui.component.plotly.traces.PlSunBurstTrace;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.importers.IImportRow;
import to.etc.webapp.query.QDataContext;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class PlotlySunburst extends UrlPage {
	@Override
	public void createContent() throws Exception {
		PlotlyGraph.initialize(this);
		add(new HTag(1, "Plotly - Sunburst chart"));

		PlotlyGraph graph = new PlotlyGraph();
		add(graph);
		graph.setHeight("800px");
		graph.setWidth("800px");
		graph.setDisplay(DisplayType.INLINE_BLOCK);
		graph.setSource(new PlotlySunnySource());
	}

	static public final class PlotlySunnySource implements IPlotlyDataSource {
		@NonNull
		@Override
		public IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception {
			ImportDataset ids = ImportDataset.createFromResource("/plotly/coffee-flavors.csv", "csv", StandardCharsets.UTF_8);

			PlotlyDataSet ds = new PlotlyDataSet();
			PlSunBurstTrace trace = ds.addSunburst();

			for(IImportRow row : ids) {
				String id = row.get("ids").getStringValue();
				String label = row.get("labels").getStringValue();
				String parent = row.get("parents").getStringValue();

				trace.add(id, parent, label);				// Valueless
			}

			return ds;
		}
	}
}
