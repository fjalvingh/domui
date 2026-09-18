package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Makes the cells the user asks for. A drawing the user can add to needs one: the browser
 * never invents a cell, it says what the user did and this decides what the model becomes.
 *
 * <pre>
 * panel.setCreateHandler(new IGraphCreateHandler() {
 *     public GraphNode createNode(GraphModel model, GraphPaletteItem item, double x, double y) {
 *         return item.create(model, x, y);
 *     }
 *     public GraphEdge createEdge(GraphModel model, GraphNode source, GraphNode target) {
 *         return source == target ? null : model.addEdge(source, target);
 *     }
 * });
 * </pre>
 *
 * <p>A method makes the cell in the model and returns it. Making nothing - and returning
 * null - is how a drawing says what may not be drawn in it, and it costs nothing to
 * refuse: the browser has nothing to take back, because it never made anything. Both
 * methods make nothing by default, so a palette item nothing answers for does nothing.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IGraphCreateHandler {
	/**
	 * The user dropped a palette item at this place in the drawing's own coordinates.
	 */
	@Nullable
	default GraphNode createNode(GraphModel model, GraphPaletteItem item, double x, double y) throws Exception {
		return null;
	}

	/**
	 * The user drew a connection from one node to another. Both ends are always there: a
	 * connection that ends on nothing is not offered.
	 */
	@Nullable
	default GraphEdge createEdge(GraphModel model, GraphNode source, GraphNode target) throws Exception {
		return null;
	}
}
