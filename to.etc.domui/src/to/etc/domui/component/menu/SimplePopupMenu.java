package to.etc.domui.component.menu;

import java.util.*;

import to.etc.domui.component.menu.PopupMenu.Item;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * EXPERIMENTAL, INCOMPLETE A popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2011
 */
public class SimplePopupMenu extends Div {
	private PopupMenu m_source;

	private NodeBase m_relativeTo;

	private Object m_targetObject;

	private String m_menuTitle;

	final private List<Item> m_actionList;

	public SimplePopupMenu() {
		m_actionList = new ArrayList<Item>();
	}

	SimplePopupMenu(NodeBase b, PopupMenu pm, List<Item> actionList, Object target) {
		m_relativeTo = b;
		m_actionList = Collections.unmodifiableList(actionList);
		m_source = pm;
		m_targetObject = target;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pmnu");
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("ui-pmnu-ttl");
		ttl.add(m_menuTitle == null ? "Menu" : m_menuTitle);

		for(Item a : m_actionList) {
			if(a.getAction() != null)
				renderAction(a.getAction());
			else
				renderItem(a);
		}

		//		appendCreateJS("WebUI.registerPopinClose('" + getActualID() + "');");
		appendCreateJS("WebUI.popupMenuShow('#" + m_relativeTo.getActualID() + "', '#" + getActualID() + "');");
	}

	public void addAction(IUIAction< ? > action) {
		m_actionList.add(new Item(action));
	}

	public void addItem(String caption, String icon, String hint, boolean disabled, IClicked<SimplePopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, hint, disabled, clk));
	}

	public void addItem(String caption, String icon, IClicked<SimplePopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, null, false, clk));
	}

	private void renderItem(final Item a) {
		Div d = renderItem(a.getTitle(), a.getHint(), a.getIcon());
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				closeMenu();
				if(null != a.getClicked())
					a.getClicked().clicked(SimplePopupMenu.this);
			}
		});
	}

	private Div renderItem(String text, String hint, String icon) {
		Div d = new Div();
		add(d);
		d.setCssClass("ui-pmnu-action");
		if(null != icon) {
			d.setBackgroundImage(icon);
		}
		d.add(text);
		if(null != hint)
			d.setTitle(hint);
		return d;
	}

	private <T> void renderAction(final IUIAction<T> action) throws Exception {
		final T val = (T) m_targetObject;
		String disa = action.getDisableReason(val);
		Div d = renderItem(action.getName(val), disa, action.getIcon(val));
		if(null != disa)
			return;
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				closeMenu();
				action.execute(m_relativeTo, val);
			}
		});
	}

	public void closeMenu() {
		if(!isAttached())
			return;
		getPage().clearPopIn();
		forceRebuild();
		remove();
	}

	@Override
	public void componentHandleWebAction(RequestContextImpl ctx, String action) throws Exception {
		if("POPINCLOSE?".equals(action)) {
			closeMenu();
		} else
			super.componentHandleWebAction(ctx, action);
	}


	PopupMenu getSource() {
		return m_source;
	}

	public Object getTargetObject() {
		return m_targetObject;
	}

}
