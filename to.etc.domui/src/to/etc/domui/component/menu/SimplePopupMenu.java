package to.etc.domui.component.menu;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.menu.PopupMenu.Item;
import to.etc.domui.component.menu.PopupMenu.Submenu;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * EXPERIMENTAL, INCOMPLETE A popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2011
 */
public class SimplePopupMenu extends PopupMenuBase {
	private PopupMenu m_source;

	private String m_menuTitle;

	public SimplePopupMenu() {
	}

	SimplePopupMenu(NodeBase b, PopupMenu pm, List<Item> actionList, Object target) {
		super(null, b, actionList, target);
		m_source = pm;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pmnu");
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("ui-pmnu-ttl");
		ttl.add(m_menuTitle == null ? "Menu" : m_menuTitle);

		addItemDiv();
		for(Item a : getActionList()) {
			if(a instanceof Submenu) {
				renderSubmenu((Submenu) a);
			} else if(a.getAction() != null) {
				renderAction(a.getAction());
			} else {
				renderItem(a);
			}
		}

		//		appendCreateJS("WebUI.registerPopinClose('" + getActualID() + "');");
		appendCreateJS("WebUI.popupMenuShow('#" + getRelativeTo().getActualID() + "', '#" + getActualID() + "');");
	}

	public void addAction(IUIAction< ? > action) {
		getActionList().add(new Item(action));
	}

	public void addItem(String caption, String icon, String hint, boolean disabled, IClicked<NodeBase> clk) {
		getActionList().add(new Item(icon, caption, hint, disabled, clk));
	}

	public void addItem(String caption, String icon, IClicked<NodeBase> clk) {
		getActionList().add(new Item(icon, caption, null, false, clk));
	}

	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("POPINCLOSE?".equals(action)) {
			closeMenu();
		} else
			super.componentHandleWebAction(ctx, action);
	}

	PopupMenu getSource() {
		return m_source;
	}

	@Override
	protected void clearPopinIf() {
		getPage().clearPopIn();
	}

}
