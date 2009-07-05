package to.etc.domui.util;

/**
 * This interface is present on node types that have the possibility to
 * be used as drag 'n drop <i>drop targets</i>, i.e. that can receive a
 * node being dragged. This is present on specific HTML DOM nodes only.
 * The interface itself does not indicate that dropping is allowed; it
 * only exposes the setter and getter for an IDropHandler interface. Only
 * when an instance of this interface is set on the node implementing this
 * interface will it be able to accept dropped nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public interface IDropTargetable {
	/**
	 * Make this node acceptable for dropping dragged items into. The handler specified handles
	 * the actual drop events and drop accept events. When set to null this node will no longer
	 * accept dropped thingerydoo's.
	 *
	 * @param handler
	 */
	public void setDropHandler(IDropHandler handler);

	/**
	 * Return the current drop handler for a node. If null the node does not accept dropped
	 * thingerydoo's.
	 * @return
	 */
	public IDropHandler getDropHandler();
}
