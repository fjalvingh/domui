package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition for a popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 6, 2011
 */
public class PopupMenu {

	/**
	 * Indicates that menu is not closed when mouse is not over it.
	 */
	private boolean permanent;

	private String stylePrefix;

	private boolean modal;

	public PopupMenu() {
		this(false, "ui-pmnu", false);
	}

	public PopupMenu(boolean permanent, String stylePrefix, boolean modal) {
		this.permanent = permanent;
		this.stylePrefix = stylePrefix;
		this.modal = modal;
	}

	public static class Item {
		private IIconRef m_icon;

		private String m_title;

		private String m_hint;

		private boolean m_disabled;

		private IClicked<NodeBase> m_clicked;

		private IUIAction< ? > m_action;

		private Submenu m_parent;

		public Item(IIconRef icon, @NonNull String title, String hint, boolean disabled, IClicked<NodeBase> clicked, Submenu parent) {
			m_icon = icon;
			m_title = title;
			m_hint = hint;
			m_disabled = disabled;
			m_clicked = clicked;
			m_parent = parent;
		}

		public Item(IUIAction< ? > action) {
			m_action = action;
		}

		public IIconRef getIcon() {
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

		public IClicked<NodeBase> getClicked() {
			return m_clicked;
		}

		public IUIAction< ? > getAction() {
			return m_action;
		}

		@Nullable
		public Submenu getParent() {
			return m_parent;
		}
	}

	public final class Submenu extends Item {
		@NonNull
		final private List<Item> m_itemList = new ArrayList<Item>();

		@Nullable
		final private Object m_target;

		public Submenu(IIconRef icon, @NonNull String title, String hint, boolean disabled, Object target, Submenu parent) {
			super(icon, title, hint, disabled, null, parent);
			m_target = target;
		}

		public void addAction(@NonNull IUIAction< ? > action) {
			m_itemList.add(new Item(action));
		}

		public void addItem(@NonNull String caption, IIconRef icon, String hint, boolean disabled, IClicked<NodeBase> clk) {
			m_itemList.add(new Item(icon, caption, hint, disabled, clk, this));
		}

		public void addItem(@NonNull String caption, IIconRef icon, IClicked<NodeBase> clk) {
			m_itemList.add(new Item(icon, caption, null, false, clk, this));
		}

		public void addItem(@NonNull Item item) {
			m_itemList.add(item);
		}

		public void addMenu(@NonNull String caption, IIconRef icon, String hint, boolean disabled, Object target) {
			m_itemList.add(new Submenu(icon, caption, hint, disabled, target, this));
		}

		@NonNull
		public List<Item> getItemList() {
			return m_itemList;
		}

		@Nullable
		public Object getTarget() {
			return m_target;
		}
	}

	private List<Item> m_actionList = new ArrayList<Item>();

	public void addAction(@NonNull IUIAction< ? > action) {
		m_actionList.add(new Item(action));
	}

	public void addItem(@NonNull String caption, IIconRef icon, String hint, boolean disabled, IClicked<NodeBase> clk) {
		m_actionList.add(new Item(icon, caption, hint, disabled, clk, null));
	}

	public void addItem(@NonNull String caption, IIconRef icon, IClicked<NodeBase> clk) {
		addItem(caption, icon, null, false, clk);
	}

	public void addItem(Item item) {
		m_actionList.add(item);
	}

	@NonNull
	public Submenu addMenu(@NonNull String caption, IIconRef icon, String hint, boolean disabled, Object target) {
		Submenu submenu = new Submenu(icon, caption, hint, disabled, target, null);
		m_actionList.add(submenu);
		return submenu;
	}

	@NonNull
	public Submenu addMenu(@NonNull String caption) {
		return addMenu(caption, null, null, false, null);
	}

	public <T> void show(NodeBase ref, T target) {
		NodeContainer nc = ref.getPage().getPopIn();
		if(nc instanceof SimplePopupMenu) {
			SimplePopupMenu sp = (SimplePopupMenu) nc;
			if(sp.getSource() == this && target == sp.getTargetObject()) {
				sp.closeMenu();
				return;
			}
		}

		SimplePopupMenu sp = new SimplePopupMenu(ref, this, m_actionList, target, stylePrefix, modal);
		ref.getPage().setPopIn(sp);
		ref.getPage().getBody().add(0, sp);
	}

	public void show(NodeBase ref) {
		show(ref, null);
	}

	public boolean isPermanent() {
		return permanent;
	}

	public boolean hasItems() {
		return m_actionList.size() > 0;
	}

}
