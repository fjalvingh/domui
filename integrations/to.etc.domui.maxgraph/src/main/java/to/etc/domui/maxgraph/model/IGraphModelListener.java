package to.etc.domui.maxgraph.model;

/**
 * Told about every change to a {@link GraphModel} it is registered on, as the change is
 * made. A {@code to.etc.domui.maxgraph.MaxGraphPanel} listens like this to collect what
 * has to be sent to the browser at the end of the request.
 *
 * <p>A model only records changes while something is listening, so a drawing that is
 * merely being built costs nothing.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IGraphModelListener {
	/**
	 * Called after the change was made to the model. The operation names the cell, so
	 * reading it gives the state the model is in now, not the state it changed from.
	 */
	void onGraphChanged(GraphModel model, GraphOp op);
}
