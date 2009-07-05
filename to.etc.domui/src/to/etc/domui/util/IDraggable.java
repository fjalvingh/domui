package to.etc.domui.util;

/**
 * This interface handles drag-related events for objects. Objects that can be
 * dragged must have been associated with an IDraggable instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public interface IDraggable {
	/**
	 * Set the drag handler to use for this thingy. When set (no an actual instance) this 
	 * means the associated class can be dragged. When set to null the instance is undraggable.
	 * @param dh
	 */
	public void setDragHandler(IDragHandler dh);

	/**
	 * Returns the current handler for dragging. If null this node cannot be dragged.
	 * @return
	 */
	public IDragHandler getDragHandler();
}
