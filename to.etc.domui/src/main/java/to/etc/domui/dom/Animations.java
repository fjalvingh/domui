package to.etc.domui.dom;

import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IExecute;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-17.
 */
@DefaultNonNull
final public class Animations {
	private Animations() {
	}

	static private class NodeFixer implements IExecute {
		final private NodeBase m_node;

		final private DisplayType m_newDisplay;

		public NodeFixer(NodeBase node, DisplayType newDisplay) {
			m_node = node;
			m_newDisplay = newDisplay;
		}

		@Override public boolean equals(@Nullable Object obj) {
			if(! (obj instanceof NodeFixer))
				return false;
			return ((NodeFixer) obj).m_node == m_node;
		}

		@Override public void execute() throws Exception {
			m_node.setDisplay(m_newDisplay);
			m_node.internalClearDelta();
			m_node.setCachedStyle(null);
			m_node.getPage().removeAfterRenderListener(this);
		}
	}

	/**
	 * Slide the specified node "down", as follows:
	 * <ul>
	 *     <li>Set the node rendered to the browser to display: none</li>
	 *     <li>Call JQuery's slideDown method</li>
	 * </ul>
	 * Since after the slideDown the node in the browser will have its default display this is also the state
	 * in DomUI's node.
	 *
	 * @param node
	 */
	static public void slideDown(NodeBase node) {
		node.getPage().addAfterRenderListener(new NodeFixer(node, node.getDisplay()));
		node.setDisplay(DisplayType.NONE);
		node.appendStatement()
			.select(node)
			.method("slideDown").end()
			.endmethod()
			.next();
	}

	/**
	 * Slide the node UP. After the slide the node will still exist but with DISPLAY:NONE. Use {@link #slideUpAndRemove(NodeBase)}
	 * to slide up, then destroy a node.
	 */
	static public void slideUp(NodeBase node) {
		node.getPage().addAfterRenderListener(new NodeFixer(node, DisplayType.NONE));
		node.appendStatement()
			.select(node)
			.method("slideUp").end()
			.endmethod()
			.next();
	}

	static public void slideUpAndRemove(NodeBase node) {
		node.getPage().addAfterRenderListener(new NodeFixer(node, DisplayType.NONE));
		node.getPage().addRemoveAfterRenderNode(node);
		node.appendStatement()
			.select(node)
			.append(".slideUp(function() { $(this).remove(); })")
			.next();
	}

}
