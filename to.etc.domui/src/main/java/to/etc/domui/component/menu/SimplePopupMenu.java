package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.component.menu.PopupMenu.Item;
import to.etc.domui.component.menu.PopupMenu.Submenu;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.server.RequestContextImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EXPERIMENTAL, INCOMPLETE A popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2011
 */
public class SimplePopupMenu extends FloatingDiv {
	private PopupMenu m_source;

	private String m_menuTitle;

	@NonNull
	private NodeBase m_relativeTo;

	private Object m_targetObject;

	private String stylePrefix;

	final private List<Item> m_actionList;

	static private class MenuLevel {
		private Submenu m_submenu;

		private Div m_div;

		private Div m_selDiv;

		public MenuLevel(Submenu submenu, Div div, Div selDiv) {
			m_submenu = submenu;
			m_div = div;
			m_selDiv = selDiv;
		}

		public Submenu getSubmenu() {
			return m_submenu;
		}

		public Div getDiv() {
			return m_div;
		}

		public Div getSelDiv() {
			return m_selDiv;
		}
	}

	private List<MenuLevel> m_stack = new ArrayList<>();

	public SimplePopupMenu(@NonNull NodeBase relativeTo) {
		m_actionList = new ArrayList<>();
		m_relativeTo = relativeTo;
	}

	SimplePopupMenu(@NonNull NodeBase b, PopupMenu pm, List<Item> actionList, Object target, String stylePrefix, boolean modal) {
		super(modal);
		m_actionList = Collections.unmodifiableList(actionList);
		m_targetObject = target;
		m_relativeTo = b;
		m_source = pm;
		this.stylePrefix = stylePrefix;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass(stylePrefix);

		Div root = new Div();
		add(root);
		root.setCssClass(stylePrefix + "-sm");

		addMenuTitle(root);

		Div items = new Div();
		root.add(items);
		for(Item a : getActionList()) {
			if(a instanceof Submenu) {
				renderSubmenu(items, (Submenu) a);
			} else if(a.getAction() != null) {
				renderAction(items, (IUIAction<Object>) a.getAction(), m_targetObject);
			} else {
				renderItem(items, a);
			}
		}

		//		appendCreateJS("WebUI.registerPopinClose('" + getActualID() + "');");
		appendCreateJS("WebUI.popupMenuShow('#" + getRelativeTo().getActualID() + "', '#" + getActualID() + "');");
	}

	private void addMenuTitle(Div root) {
		String menuTitle = m_menuTitle;
		if(null != menuTitle) {
			Div ttl = new Div();
			root.add(ttl);
			ttl.setCssClass(stylePrefix + "-ttl");
			ttl.add(menuTitle);
		}
	}

	private void setSubmenuSelected(Div selectDiv, boolean on, int level) {
		Img img = selectDiv.getChildren(Img.class).get(0);
		if(on) {
//			selectDiv.addCssClass(stylePrefix + "-subsel");
			selectDiv.addCssClass(stylePrefix + "-sm" + level);
			img.setSrc("THEME/pmnu-submenu-close.png");
		} else {
//			selectDiv.removeCssClass(stylePrefix + "-subsel");
			selectDiv.removeCssClass(stylePrefix + "-sm" + level);
			img.setSrc("THEME/pmnu-submenu-open.png");
		}
	}

	protected void submenuClicked(Div selectDiv, Submenu s) throws Exception {
		//-- If the item clicked is the top level one- just discard it,
		if(!m_stack.isEmpty() && m_stack.get(m_stack.size() - 1).getSubmenu() == s) {
			MenuLevel level = m_stack.remove(m_stack.size() - 1);
			level.getDiv().remove();
			setSubmenuSelected(level.getSelDiv(), false, m_stack.size() + 1);
			return;
		}

		//-- Wind back the stack till the parent of the wanted menu
		while(!m_stack.isEmpty()) {
			MenuLevel level = m_stack.get(m_stack.size() - 1);
			if(level.getSubmenu() == s.getParent())
				break;

			m_stack.remove(m_stack.size() - 1);
			level.getDiv().remove();
			setSubmenuSelected(level.getSelDiv(), false, m_stack.size() + 1);
		}

		//-- Now add the new level,
		Div root = new Div();
		selectDiv.add(root);

		root.setCssClass(stylePrefix + "-sm " + stylePrefix + "-sm" + (m_stack.size() + 1));

		addMenuTitle(root);

		Div items = new Div();
		root.add(items);
		for(Item a : s.getItemList()) {
			if(a instanceof Submenu) {
				renderSubmenu(items, (Submenu) a);
			} else if(a.getAction() != null) {
				Object target = null;
				if(s.getTarget() != null)
					target = s.getTarget();

				renderAction(items, (IUIAction<Object>) a.getAction(), target);
			} else {
				renderItem(items, a);
			}
		}
		setSubmenuSelected(selectDiv, true, m_stack.size() + 1);

		m_stack.add(new MenuLevel(s, root, selectDiv));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Renderers.											*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param a
	 */
	protected void renderSubmenu(@NonNull NodeContainer into, final Submenu a) {
		final Div d = renderItem(into, a.getTitle(), a.getHint(), a.getIcon(), false);
		Img img = new Img("THEME/pmnu-submenu-open.png");
		d.add(img);
		d.setClicked(clickednode -> submenuClicked(d, a));
	}

	protected void renderItem(@NonNull NodeContainer into, final Item a) {
		Div d = renderItem(into, a.getTitle(), a.getHint(), a.getIcon(), false);
		d.setClicked(clickednode -> {
			closeMenu();
			if(null != a.getClicked())
				a.getClicked().clicked(SimplePopupMenu.this);
		});
	}

	private Div renderItem(@NonNull NodeContainer into, String text, String hint, IIconRef icon, boolean disabled) {
		Div d = new Div();
		into.add(d);
		d.setCssClass(stylePrefix + "-action " + (disabled ? stylePrefix + "-disabled" : stylePrefix + "-enabled"));
		if(null != icon) {
			NodeBase node = icon.createNode();
			node.addCssClass(stylePrefix + "-icon");
			d.add(node);
		}
		d.add(new Span(stylePrefix + "-icon", text));
		if(null != hint)
			d.setTitle(hint);
		return d;
	}

	protected <T> void renderAction(@NonNull NodeContainer into, final IUIAction<T> action, final T val) throws Exception {
		String disa = action.getDisableReason(val);
		if(null != disa) {
			renderItem(into, action.getName(val), disa, action.getIcon(val), true);
			return;
		}

		Div d = renderItem(into, action.getName(val), action.getTitle(val), action.getIcon(val), false);
		d.setClicked(clickednode -> {
			closeMenu();
			action.execute(getRelativeTo(), val);
		});
	}

	public void closeMenu() {
		if(!isAttached())
			return;
		clearPopinIf();
		forceRebuild();
		appendJavascript("WebUI.popinClosed('#" + getActualID() + "');");
		remove();
	}

	protected List<Item> getActionList() {
		return m_actionList;
	}

	private NodeBase getRelativeTo() {
		return m_relativeTo;
	}

	public void addAction(IUIAction< ? > action) {
		getActionList().add(new Item(action));
	}

	public void addItem(String caption, IIconRef icon, String hint, boolean disabled, IClicked<NodeBase> clk) {
		getActionList().add(new Item(icon, caption, hint, disabled, clk, null));
	}

	public void addItem(String caption, IIconRef icon, IClicked<NodeBase> clk) {
		getActionList().add(new Item(icon, caption, null, false, clk, null));
	}

	@Override
	public void componentHandleWebAction(@NonNull RequestContextImpl ctx, @NonNull String action) throws Exception {
		if("POPINCLOSE?".equals(action)) {
			if (!getSource().isPermanent()) {
				closeMenu();
			}
		} else
			super.componentHandleWebAction(ctx, action);
	}

	PopupMenu getSource() {
		return m_source;
	}

	public Object getTargetObject() {
		return m_targetObject;
	}

	protected void clearPopinIf() {
		getPage().clearPopIn();
	}

	@Override
	public void closePressed() {
		closeMenu();
	}

}
