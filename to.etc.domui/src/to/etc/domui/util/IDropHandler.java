package to.etc.domui.util;

/**
 * Nodes that can accept dropped things must have this interface defined via setDropHandler(). The instance
 * of this handler determines what happens with the dropped node, and which nodes are acceptable for dropping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2008
 */
public interface IDropHandler {
	/**
	 * Returns the list of types acceptable for this target. Only draggable's that have
	 * one of these types will be accepted. This MUST return a string array containing at
	 * least one string; if not an exception occurs as soon as this gets used.
	 * @return	a non-null minimal length=1 String array containing the types that are acceptable for this drop zone.
	 */
	public String[] getAcceptableTypes();

	/**
	 * This is an event function which gets called as soon as a Draggable is dropped on the dropTarget
	 * having this handler. This event gets called <i>after</i> IDragHandler.onDropped() has been called
	 * for the dropped draggable.
	 *
	 * @param context
	 * @throws Exception
	 */
	public void onDropped(DropEvent context) throws Exception;
}
