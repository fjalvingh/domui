package to.etc.domui.util;

import to.etc.domui.dom.html.*;

/**
 * Interface must be used for a drag and drop implementation, where the dragHandle is not the only
 * node to be dragged. This interface is used to be able to work in javascript with the actual node
 * that is dragged. The dragarea knowns wich element handles the actual drag and drop implementation.
 *
 * Created on Feb 11, 2011
 */
public interface IDragArea {

	/**
	 * Returns the handle for the drag and drop functionality.
	 * @return
	 */
	IDraggable getDragHandle();

	/**
	 * Will normally be implemented by the nodebase {@link NodeBase#getActualID()}
	 * @return the id of this node in the generated HTML
	 */
	String getActualID();

}
