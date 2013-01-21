package to.etc.domui.component.menu;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.menu.PopupMenu.Item;
import to.etc.domui.component.menu.PopupMenu.Submenu;
import to.etc.domui.dom.html.*;

class PopupMenuBase extends Div {
	private NodeBase m_relativeTo;

	private Object m_targetObject;

	final private List<Item> m_actionList;

	private PopupSubmenu m_currentSub;

	private Div m_itemdiv;

	final private SimplePopupMenu m_rootMenu;

	PopupMenuBase() {
		m_actionList = new ArrayList<Item>();
		m_rootMenu = null;
	}

	PopupMenuBase(SimplePopupMenu root, NodeBase b, List<Item> actionList, Object target) {
		m_relativeTo = b;
		m_actionList = Collections.unmodifiableList(actionList);
		m_targetObject = target;
		m_rootMenu = root;
	}

	protected List<Item> getActionList() {
		return m_actionList;
	}

	protected void addItemDiv() {
		m_itemdiv = new Div();
		add(m_itemdiv);
		m_itemdiv.setCssClass("ui-pmnu-items");
	}

	protected void renderSubmenu(final Submenu a) {
		final Div d = renderItem(a.getTitle(), a.getHint(), a.getIcon(), false);
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				//-- Need to show the submenu.
				renderSubmenuOpen(d, a);
			}
		});
	}

	private void renderSubmenuOpen(@Nonnull Div parentd, @Nonnull Submenu a) {
		if(m_currentSub != null) {
			boolean same = a == m_currentSub.getMenu();
			m_currentSub.closeMenu();
			m_currentSub.getRelativeTo().removeCssClass("ui-pmnu-subsel");
			m_currentSub = null;
			if(same)
				return;
		}

		parentd.addCssClass("ui-pmnu-subsel");
		PopupSubmenu psu = new PopupSubmenu(m_rootMenu, this, parentd, a);
		getPage().getBody().add(psu);
		m_currentSub = psu;
	}

	protected void renderItem(final Item a) {
		Div d = renderItem(a.getTitle(), a.getHint(), a.getIcon(), false);
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				closeMenu();
				if(null != a.getClicked())
					a.getClicked().clicked(PopupMenuBase.this);
			}
		});
	}

	private Div renderItem(String text, String hint, String icon, boolean disabled) {
		Div d = new Div();
		m_itemdiv.add(d);
		d.setCssClass("ui-pmnu-action " + (disabled ? "ui-pmnu-disabled" : "ui-pmnu-enabled"));
		if(null != icon) {
			d.setBackgroundImage(icon);
		}
		d.add(text);
		if(null != hint)
			d.setTitle(hint);
		return d;
	}

	protected <T> void renderAction(final IUIAction<T> action) throws Exception {
		final T val = (T) m_targetObject;
		String disa = action.getDisableReason(val);
		if(null != disa) {
			Div d = renderItem(action.getName(val), disa, action.getIcon(val), true);
			return;
		}

		Div d = renderItem(action.getName(val), action.getTitle(val), action.getIcon(val), false);
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(@Nonnull NodeBase clickednode) throws Exception {
				closeMenu();
				action.execute(m_relativeTo, val);
			}
		});
	}

	public void closeMenu() {
		if(!isAttached())
			return;
		if(m_currentSub != null) {
			m_currentSub.closeMenu();
			m_currentSub = null;
		}
		clearPopinIf();
		forceRebuild();
		remove();
	}

	protected void clearPopinIf() {}

	public Object getTargetObject() {
		return m_targetObject;
	}

	public NodeBase getRelativeTo() {
		return m_relativeTo;
	}
}
