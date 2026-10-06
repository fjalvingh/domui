package to.etc.domui.component.headers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.CloseOnClickPanel;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.function.IExecute;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/21/15.
 */
@NonNullByDefault
public class HamburgerMenu extends CloseOnClickPanel {
	final private List<IUIAction> m_actionList;

	@Nullable
	private INotify<IUIAction> m_onSelection;

	/** What opened the menu; the menu is shown against it. Defaults to the node the menu was appended after. */
	@Nullable
	private NodeBase m_anchor;

	public HamburgerMenu(List<IUIAction> actionList) {
		m_actionList = actionList;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-hmbrg-menu");

		boolean hasicon = false;
		for(IUIAction action : m_actionList) {
			if(action.getIcon() != null) {
				hasicon = true;
			}
		}

		for(IUIAction action : m_actionList) {
			renderAction(action, hasicon);
		}
		super.createContent();

		NodeBase anchor = findAnchor();
		if(null != anchor) {
			//-- Just below what opened it, left-aligned with it - or right-aligned with it when that would run past the window's right edge.
			appendCreateJS("(function(){var a=$('#" + anchor.getActualID() + "'),m=$('#" + getActualID() + "');"
				+ "var o=a.offset(),l=o.left;"
				+ "if(l+m.outerWidth()>$(window).scrollLeft()+$(window).width()-4)l=Math.max(0,o.left+a.outerWidth()-m.outerWidth());"
				+ "m.css({right:'auto'});m.offset({top:o.top+a.outerHeight()+2,left:l});})();");
		}
	}

	@Nullable
	private NodeBase findAnchor() {
		NodeBase anchor = m_anchor;
		if(null != anchor)
			return anchor;
		if(!hasParent())
			return null;
		int ix = getParent().findChildIndex(this);
		return ix > 0 ? getParent().getChild(ix - 1) : null;
	}

	/**
	 * Show the menu against this node instead of against the node it was appended after.
	 */
	public void setAnchor(@Nullable NodeBase anchor) {
		m_anchor = anchor;
	}

	private void renderAction(IUIAction action, boolean hasicon) throws Exception {
		Div sel = new Div();
		add(sel);
		sel.setCssClass("ui-hmbrg-item" + (hasicon ? " ui-hmbrg-icon" : ""));
		IIconRef icon = action.getIcon();

		String disable = action.getDisableReason();
		if(null != icon) {
			NodeBase node = icon.createNode();
			node.addCssClass("ui-hmbrg-icon");
			sel.add(node);
		}
		Span sp = new Span("ui-hmbrg-txt", action.getName());
		sel.add(sp);
		if(null != disable) {
			sel.addCssClass("ui-hmbrg-disabled ui-disabled");
			sel.setTitle(disable);
		} else {
			sel.setClicked(()-> handleSelection(action));
		}
	}

	private void handleSelection(IUIAction action) throws Exception {
		close();

		INotify<IUIAction> onSelection = m_onSelection;
		if(null != onSelection)
			onSelection.onNotify(action);
	}

	/**
	 * Sets a notification to be called when a menu selection is made.
	 * @return
	 */
	@Nullable
	public INotify<IUIAction> getOnSelection() {
		return m_onSelection;
	}

	public void setOnSelection(@Nullable INotify<IUIAction> onSelection) {
		m_onSelection = onSelection;
	}
}
