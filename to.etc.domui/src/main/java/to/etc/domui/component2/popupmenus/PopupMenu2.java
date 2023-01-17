package to.etc.domui.component2.popupmenus;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.server.RequestContextImpl;
import to.etc.function.IExecute;
import to.etc.webapp.nls.IBundleCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-06-2022.
 */
public class PopupMenu2 extends Div {
	private final NodeContainer m_owner;

	@Nullable
	private String m_text;

	@Nullable
	private IIconRef m_iconRef;

	@Nullable
	private String m_hint;

	@Nullable
	private IExecute m_clicked;

	@Nullable
	private String m_disableReason;

	private final List<Item> m_itemList = new ArrayList<>();

	public PopupMenu2(NodeContainer owner) {
		m_owner = owner;
	}

	public void show(NodeContainer container) {
		container.getPage().getBody().add(0, this);
	}

	@Override
	public void createContent() throws Exception {
		addCssClass("ui-pome2");
		if(m_iconRef != null || m_hint != null || m_text != null)
			throw new IllegalStateException("You are missing a call to append()");
		appendCreateJS("WebUI.popupMenuShow('#" + m_owner.getActualID() + "', '#" + getActualID() + "', 'below');");

		boolean hasIcons = false;
		boolean hasTexts = false;
		for(Item item : m_itemList) {
			if(item.getText() != null)
				hasTexts = true;
			if(item.getIcon() != null)
				hasIcons = true;
		}
		for(Item item : m_itemList) {
			addItem(item, hasIcons, hasTexts, item.getDisableReason());
		}
	}

	private void addItem(Item item, boolean hasIcons, boolean hasTexts, @Nullable String disableReason) {
		Div row = new Div("ui-pome2-r");
		add(row);
		if(null == disableReason) {
			row.setClicked(a -> selectionMade(item));
		}
		if(hasIcons) {
			Div icd = new Div("ui-pome2-i");
			row.add(icd);
			IIconRef icon = item.getIcon();
			if(null != icon) {
				icd.add(icon.createNode());
			}
		}

		if(hasTexts) {
			Div txd = new Div("ui-pome2-t");
			row.add(txd);
			String text = item.getText();
			if(null != text) {
				txd.add(text);
			}
		}

		String hint = item.getHint();
		if(null != disableReason) {
			row.addCssClass("disabled");
			if(null == hint) {
				hint = disableReason;
			}else {
				hint += " - " + disableReason;
			}
		}

		row.setTitle(hint);
	}

	private void selectionMade(Item item) throws Exception {
		appendJavascript("WebUI.popinClosed('#" + getActualID() + "');");		// ORDERED 1
		remove();																// ORDERED 2
		IExecute clicked = item.getClicked();
		if(null != clicked) {
			clicked.execute();
		}
	}

	public PopupMenu2 text(IBundleCode code, Object... para) {
		checkEmpty(m_text);
		m_text = code.format(para);
		return this;
	}

	public PopupMenu2 text(String text) {
		checkEmpty(m_text);
		m_text = text;
		return this;
	}

	public PopupMenu2 hint(IBundleCode code, Object... para) {
		checkEmpty(m_hint);
		m_hint = code.format(para);
		return this;
	}

	public PopupMenu2 hint(String hint) {
		checkEmpty(m_hint);
		m_hint = hint;
		return this;
	}

	public PopupMenu2 icon(IIconRef icon) {
		checkEmpty(m_iconRef);
		m_iconRef = icon;
		return this;
	}

	public PopupMenu2 click(IExecute c) {
		checkEmpty(m_clicked);
		m_clicked = c;
		return this;
	}

	public PopupMenu2 disableReason(String disableReason) {
		checkEmpty(m_disableReason);
		m_disableReason = disableReason;
		return this;
	}

	public PopupMenu2 append() {
		if(m_iconRef == null && m_text == null)
			throw new IllegalStateException("No text nor an icon set; one of the two is mandatory");
		m_itemList.add(new Item(m_text, m_iconRef, m_hint, m_clicked, m_disableReason));
		m_text = null;
		m_hint = null;
		m_iconRef = null;
		m_clicked = null;
		m_disableReason = null;
		forceRebuild();
		return this;
	}

	private void checkEmpty(@Nullable Object what) {
		if(null != what)
			throw new IllegalStateException("Value already set; have you forgotten to call append() to add the item?");
	}

	/**
	 * Gets called when the popin must be closed (when clicking anywhere else on the page).
	 */
	public void webActionPOPINCLOSE(@NonNull RequestContextImpl ctx) throws Exception {
		remove();
	}

	static private class Item {
		@Nullable
		private final String m_text;

		@Nullable
		private final String m_hint;

		@Nullable
		private final IIconRef m_icon;

		@Nullable
		private final IExecute m_clicked;

		@Nullable
		private final String m_disableReason;

		public Item(@Nullable String text, @Nullable IIconRef icon, @Nullable String hint, @Nullable IExecute clicked, @Nullable String disableReason) {
			m_text = text;
			m_icon = icon;
			m_hint = hint;
			m_clicked = clicked;
			m_disableReason = disableReason;
		}

		@Nullable
		public String getHint() {
			return m_hint;
		}

		@Nullable
		public String getText() {
			return m_text;
		}

		@Nullable
		public IIconRef getIcon() {
			return m_icon;
		}

		@Nullable
		public IExecute getClicked() {
			return m_clicked;
		}

		@Nullable
		public String getDisableReason() {
			return m_disableReason;
		}
	}

}
