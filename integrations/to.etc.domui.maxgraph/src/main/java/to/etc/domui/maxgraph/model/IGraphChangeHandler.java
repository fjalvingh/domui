package to.etc.domui.maxgraph.model;

/**
 * Asked about every change a user makes to a drawing in the browser, before it is made to
 * the model.
 *
 * <pre>
 * panel.setChangeHandler(change -&gt; {
 *     if(change.getType() == GraphOpType.Remove &amp;&amp; change.getCell() == hub) {
 *         return false;                                // The hub stays.
 *     }
 *     return true;
 * });
 * </pre>
 *
 * <p>Returning false refuses the change: the model keeps what it had, and what it has is
 * sent back to the browser, so the drawing goes back to it. A page that only wants to know
 * what happened returns true and reads the change.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public interface IGraphChangeHandler {
	boolean acceptChange(GraphChange change) throws Exception;
}
