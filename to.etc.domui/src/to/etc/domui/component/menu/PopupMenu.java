package to.etc.domui.component.menu;

import java.util.*;

import to.etc.domui.dom.html.*;

/**
 * EXPERIMENTAL, INCOMPLETE A popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2011
 */
public class PopupMenu extends Div {
	private Object m_targetObject;

	private String m_menuTitle;

	private static class Item {
		private String m_icon;

		private String m_title;

		private String m_hint;

		private boolean m_disabled;

		private IClicked<PopupMenu> m_clicked;

		private IUIAction< ? > m_action;

		public Item(String icon, String title, String hint, boolean disabled, IClicked<PopupMenu> clicked) {
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

		public IClicked<PopupMenu> getClicked() {
			return m_clicked;
		}

		public IUIAction< ? > getAction() {
			return m_action;
		}
	}

	private List<Item> m_actionList = new ArrayList<Item>();

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pmnu");
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("pmnu-ttl");
		ttl.add(m_menuTitle == null ? "Menu" : m_menuTitle);

		for(Item a : m_actionList) {
			if(a.getAction() != null)
				renderAction(a.getAction());
			else
				renderItem(a);
		}
	}

	public void addAction(IUIAction< ? > action) {
		m_actionList.add(new Item(action));
	}

	public void addItem(String caption, String icon, String hint, boolean disabled, IClicked<PopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, hint, disabled, clk));
	}

	public void addItem(String caption, String icon, IClicked<PopupMenu> clk) {
		m_actionList.add(new Item(icon, caption, null, false, clk));
	}

	private void renderItem(final Item a) {
		Div d = renderItem(a.getTitle(), a.getHint(), a.getIcon());
		d.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				a.getClicked().clicked(PopupMenu.this);
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
				action.execute(val);
			}
		});
	}
}
