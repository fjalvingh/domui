package to.etc.domui.component.plotly.traces;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public interface IPlotlyTrace {
	void render(@NonNull JsonBuilder b) throws Exception;
}
