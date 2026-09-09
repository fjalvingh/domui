package to.etc.domui.maxgraph;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.server.StringBufferDataFactory;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.javascript.JsonBuilder;

/**
 * A diagram, drawn in the browser by <a href="https://github.com/maxGraph/maxGraph">maxGraph</a>
 * from a {@link GraphModel} built in Java.
 *
 * <pre>
 * MaxGraphPanel.initialize(this);
 *
 * GraphModel model = new GraphModel();
 * GraphNode start = model.addNode("Start", 40, 20, 120, 40)
 *     .styled(s -&gt; s.shape(GraphShape.Ellipse));
 * GraphNode check = model.addNode("Check the order", 40, 120, 120, 40);
 * model.addEdge(start, check);
 *
 * MaxGraphPanel panel = new MaxGraphPanel();
 * add(panel);
 * panel.size("100%", "400px").setModel(model);
 * </pre>
 *
 * <p>The panel renders one empty div and leaves everything inside it to maxGraph. The
 * drawing itself is not in the page's HTML: the browser asks for the model as JSON once
 * the component is there, and builds it. That also means the drawing must be rebuildable
 * at any time - a {@link #forceRebuild()} of the panel or of anything above it throws the
 * browser-side instance away, and the create call that comes with the next render asks
 * for the model again.</p>
 *
 * <p>A page that uses this must call {@link #initialize(NodeContainer)} once, which adds
 * the library to that page only.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MaxGraphPanel extends Div implements IComponentJsonProvider {
	/** Where the maxGraph resources sit in this module's META-INF/resources. */
	static private final String RESOURCE_ROOT = "js/maxgraph";

	private GraphModel m_model = new GraphModel();

	private boolean m_panning = true;

	public MaxGraphPanel() {
		setCssClass("ui-mxgr");
	}

	/**
	 * Add the maxGraph library to the page this node is on. Header contributors are per
	 * page, so only the pages that show a diagram load the bundle.
	 */
	static public void initialize(NodeContainer what) {
		Page page = what.getPage();
		page.addHeaderContributor(HeaderContributor.loadStylesheet("$" + RESOURCE_ROOT + "/css/common.css"), 10);
		page.addHeaderContributor(HeaderContributor.loadStylesheet("$" + RESOURCE_ROOT + "/domui-maxgraph.css"), 11);
		page.addHeaderContributor(HeaderContributor.loadJavascript("$" + RESOURCE_ROOT + "/domui-maxgraph.js"), 12);
	}

	/**
	 * The create call carries only what is needed to construct the widget: it runs again
	 * on every full page render, so it may not contain any of the model's state.
	 */
	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DomUIMaxGraph.create('").append(getActualID()).append("', {imageBase:'")
			.append(DomUtil.getRelativeApplicationResourceURL(RESOURCE_ROOT + "/images"))
			.append("'})");
		appendCreateJS(sb);
	}

	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		appendJavascript("DomUIMaxGraph.destroy('" + getActualID() + "');");
	}

	/**
	 * Called out of band by the browser side, once per created widget, to get the drawing.
	 */
	@NonNull
	@Override
	public Object provideJsonData(@NonNull IPageParameters parameterSource) throws Exception {
		StringBufferDataFactory sb = new StringBufferDataFactory("application/json");
		try(JsonBuilder b = new JsonBuilder(sb)) {
			new GraphJsonRenderer().render(b, m_model, m_panning);
		}
		return sb;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Properties											        */
	/*----------------------------------------------------------------------*/

	public GraphModel getModel() {
		return m_model;
	}

	/**
	 * Show this model. The drawing is rebuilt from it.
	 */
	public MaxGraphPanel setModel(GraphModel model) {
		m_model = model;
		forceRebuild();
		return this;
	}

	public boolean isPanning() {
		return m_panning;
	}

	/**
	 * Whether the drawing can be moved by dragging its background. On by default.
	 */
	public MaxGraphPanel setPanning(boolean panning) {
		if(m_panning == panning) {
			return this;
		}
		m_panning = panning;
		forceRebuild();
		return this;
	}

	/**
	 * Set both dimensions of the drawing area. maxGraph draws into a container that has
	 * a size of its own; without one there is nothing to see.
	 */
	public MaxGraphPanel size(String width, String height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
}
