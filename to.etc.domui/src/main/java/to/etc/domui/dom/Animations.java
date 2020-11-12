package to.etc.domui.dom;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IExecute;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-17.
 */
@NonNullByDefault
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
		slideUp(node, "");
	}

	/**
	 * Slide the node UP, with specified js callback.
	 */
	static public void slideUp(NodeBase node, String jsCallback) {
		node.getPage().addAfterRenderListener(new NodeFixer(node, DisplayType.NONE));
		node.appendStatement()
			.select(node)
			.append(".slideUp(function() { " + jsCallback + " })")
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

	static public void shake(NodeBase node) {
		node.appendStatement()
			.select(node)
			.append(".effect('shake')")
			.next();
	}

	static public void bounce(NodeBase node) {
		node.appendStatement()
			.select(node)
			.append(".effect('bounce')")
			.next();
	}

	/**
	 * Does pulsate effect.
	 * @param node target node
	 * @param times if 0 it uses default behavior for pulsate.
	 */
	static public void pulsate(NodeBase node, int times) {
		String timesOption = "";
		if(times > 0) {
			timesOption = ", " + times;
		}
		node.appendStatement()
			.select(node)
			.append(".effect('pulsate'").append(timesOption).append(")")
			.next();
	}

	static public void scrollIntoView(NodeBase node) {
		node.appendStatement()
			.select(node)
			.append("[0].scrollIntoViewIfNeeded()")
			.next();
	}
}
