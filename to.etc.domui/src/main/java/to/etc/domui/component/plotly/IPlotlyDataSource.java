package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;

/**
 * This is a datasource for Plotly graphs. It gets called OUTSIDE a page's
 * context to calculate plotly data for a graph, and must be implemented
 * by anything that produces graphs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public interface IPlotlyDataSource {
	@NonNull
	IPlotlyDataset createDataset(@NonNull QDataContext dc) throws Exception;
}
