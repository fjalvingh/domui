package to.etc.domui.dom.html;

/**
 * Listener for basic clickies on a Node. Represents the onclick handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public interface IClicked<T extends NodeBase> {
	/**
	 * This gets called when the node is clicked. The parameter is the node that the click
	 * handler was attached to. Since the node itself is passed you can easily reuse a click
	 * handler instance for several same-type nodes.
	 * @param clickednode
	 * @throws Exception
	 */
	public void clicked(T clickednode) throws Exception;
}
