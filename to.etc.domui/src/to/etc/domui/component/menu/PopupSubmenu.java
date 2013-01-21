package to.etc.domui.component.menu;

import to.etc.domui.component.menu.PopupMenu.Item;
import to.etc.domui.component.menu.PopupMenu.Submenu;
import to.etc.domui.dom.html.*;

public class PopupSubmenu extends PopupMenuBase {
	final private PopupMenuBase m_menuBase;

	private Submenu m_menu;

	public PopupSubmenu(PopupMenuBase popupMenuBase, Div parentd, Submenu a) {
		super(parentd, a.getItemList(), a.getTarget());
		m_menuBase = popupMenuBase;
		m_menu = a;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pmnu-sm");
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

		appendCreateJS("WebUI.popupSubmenuShow('#" + m_menuBase.getActualID() + "', '#" + getActualID() + "');");

	}

	public Submenu getMenu() {
		return m_menu;
	}
}
