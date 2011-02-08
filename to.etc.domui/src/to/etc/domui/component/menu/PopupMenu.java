package to.etc.domui.component.menu;

import java.util.*;

import to.etc.domui.dom.html.*;

/**
 * Definition for a popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 6, 2011
 */
public class PopupMenu {
	public static class Item {
		private String m_icon;

		private String m_title;

		private String m_hint;

		private boolean m_disabled;

		private IClicked<SimplePopupMenu> m_clicked;

		private IUIAction< ? > m_action;

		public Item(String icon, String title, String hint, boolean disabled, IClicked<SimplePopupMenu> clicked) {
			m_icon = icon;
			m_title = title;
			m_hint = hint;
			m_disabled = disabled;
			m_clicked = clicked;
		}

		public Item(IUIAction< ? > action) {
			m_action = action;
		}

		public String getIcon() {
			return m_icon;
		}

		public String getTitle() {
			return m_title;
		}

		public String getHint() {
			return m_hint;
		}

		public boolean isDisabled() {
			return m_disabled;
		}

		public IClicked<SimplePopupMenu> getClicked() {
			return m_clicked;
		}

		public IUIAction< ? > getAction() {
			return m_action;
		}
	}

	private List<Item> m_actionList = new ArrayList<Item>();

	public void addAction(IUIAction< ? > action) {
		m_actionList.add(new Item(action));
	}

	public void addItem(String caption, String icon, String hint, boolean disabled, IClicked<SimplePopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, hint, disabled, clk));
	}

	public void addItem(String caption, String icon, IClicked<SimplePopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, null, false, clk));
	}



	/**
	 *
	 * @param ref
	 * @param target
	 */
	public void show(NodeContainer ref, Object target) {
		NodeContainer nc = ref.getPage().getPopIn();
		if(nc instanceof SimplePopupMenu) {
			SimplePopupMenu sp = (SimplePopupMenu) nc;
			if(sp.getSource() == this && target == sp.getTargetObject()) {
				sp.closeMenu();
				return;
			}
		}

		SimplePopupMenu sp = new SimplePopupMenu(ref, this, m_actionList, target);
		ref.getPage().setPopIn(sp);
		ref.getPage().getBody().add(0, sp);
	}
}
