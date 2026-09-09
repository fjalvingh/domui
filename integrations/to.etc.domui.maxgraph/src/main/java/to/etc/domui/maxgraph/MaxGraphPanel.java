package to.etc.domui.maxgraph;

import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.util.DomUtil;

/**
 * A diagram, drawn in the browser by <a href="https://github.com/maxGraph/maxGraph">maxGraph</a>.
 *
 * <p>The panel renders one empty div and leaves everything inside it to maxGraph, so
 * the drawing must be rebuildable at any time: a {@link #forceRebuild()} of the panel
 * or of anything above it throws the browser-side instance away.</p>
 *
 * <p>A page that uses this must call {@link #initialize(NodeContainer)} once, which adds
 * the library to that page only.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MaxGraphPanel extends Div {
	/** Where the maxGraph resources sit in this module's META-INF/resources. */
	static private final String RESOURCE_ROOT = "js/maxgraph";

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
	 * Set both dimensions of the drawing area. maxGraph draws into a container that has
	 * a size of its own; without one there is nothing to see.
	 */
	public MaxGraphPanel size(String width, String height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
}
