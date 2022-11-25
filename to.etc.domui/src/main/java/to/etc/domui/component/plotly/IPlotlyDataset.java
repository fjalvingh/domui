package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * A plotly dataset, which is all the info needed for Plotly to draw a graph.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public interface IPlotlyDataset {
	void render(@NonNull JsonBuilder b) throws Exception;
}
