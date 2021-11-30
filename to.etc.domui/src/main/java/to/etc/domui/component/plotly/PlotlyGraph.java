package to.etc.domui.component.plotly;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.server.StringBufferDataFactory;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.javascript.JsonBuilder;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class PlotlyGraph extends Div implements IComponentJsonProvider {
	@Nullable
	private IPlotlyDataSource m_source;

	/**
	 * This is the component container. Initially we have it filled with some "empty" image while
	 * we send the javascript to obtain the dataset.
	 */
	@Override
	public void createContent() throws Exception {
		addCssClass("ui-plotly");
		add(new Div("ui-plotly-empty"));
		appendCreateJS("DomUI.plotlyComponent('" + getActualID() + "')");
	}

	@Nullable
	public IPlotlyDataSource getSource() {
		return m_source;
	}

	public void setSource(@Nullable IPlotlyDataSource source) {
		m_source = source;
		forceRebuild();
	}

	/**
	 * Callback when the plotly components would like to get its dataset.
	 */
	@NonNull
	@Override
	public Object provideJsonData(@NonNull IPageParameters parameterSource) throws Exception {
		IPlotlyDataSource source = getSource();
		if(null == source) {
			return "{}";
		}

		try(QDataContext dc = QContextManager.createUnmanagedContext()) {
			IPlotlyDataset dataset = source.createDataset(dc);
			StringBufferDataFactory sb = new StringBufferDataFactory("text/json");
			JsonBuilder b = new JsonBuilder(sb);
			renderDataset(b, dataset);
			b.close();
			return sb;
		}
	}

	private void renderDataset(JsonBuilder b, IPlotlyDataset dataset) throws Exception {
		dataset.render(b);
	}

	static public final void initialize(NodeContainer what) {
		what.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdn.plot.ly/plotly-latest.min.js"), 10);
	}

}
