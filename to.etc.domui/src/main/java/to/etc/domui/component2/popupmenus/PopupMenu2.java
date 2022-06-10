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
			addItem(item, hasIcons, hasTexts);
		}
	}

	private void addItem(Item item, boolean hasIcons, boolean hasTexts) {
		Div row = new Div("ui-pome2-r");
		add(row);
		row.setClicked(a -> selectionMade(item));
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

		row.setTitle(item.getHint());
	}

	private void selectionMade(Item item) throws Exception {
		remove();
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

	public PopupMenu2 hint(IBundleCode code, Object... para) {
		checkEmpty(m_hint);
		m_hint = code.format(para);
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

	public PopupMenu2 append() {
		if(m_iconRef == null && m_text == null)
			throw new IllegalStateException("No text nor an icon set; one of the two is mandatory");
		m_itemList.add(new Item(m_text, m_iconRef, m_hint, m_clicked));
		m_text = null;
		m_hint = null;
		m_iconRef = null;
		m_clicked = null;
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

		public Item(@Nullable String text, @Nullable IIconRef icon, @Nullable String hint, @Nullable IExecute clicked) {
			m_text = text;
			m_icon = icon;
			m_hint = hint;
			m_clicked = clicked;
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
	}

}
