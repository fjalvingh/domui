package to.etc.domui.component.headers;

import static to.etc.domui.util.DomUtil.*;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.event.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

/**
 * This is a header for a collapsable region with an expand/collapse
 * button at the front, a text in the middle and an optional "hamburger"
 * button at the end.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9/18/15.
 */
public class ExpandHeader extends Div {
	@Nullable
	private String m_caption;

	@Nullable
	private NodeBase m_captionNode;

	private List<IUIAction<?>> m_actionList = new ArrayList<>();

	@Nullable
	private HoverButton m_expandButton;

	@Nullable
	private HoverButton	m_hamburgerButton;

	private boolean m_expanded;

	final private Div m_content = new Div();

	@Nullable
	private INotify<Div> m_onExpand;

	@Nullable
	private HamburgerMenu m_menu;

	@Nullable
	private TD m_titleNode;

	public enum Type {
		NORMAL, SMALL
	}

	private Type m_type = Type.NORMAL;

	public ExpandHeader() {
	}

	public ExpandHeader(String title) {
		m_caption = title;
	}

	public ExpandHeader(Type type, String title) {
		m_caption = title;
		m_type = type;
	}

	private String getImage(boolean expanded) {
		String base = expanded ? "THEME/btnHeaderExpanded" : "THEME/btnHeaderCollapsed";
		return base + m_type.name() + ".png";
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-xphd ui-xphd-" + m_type.name().toLowerCase());
		TBody body = addTable();
		Table table = body.getTable();
		table.setTableWidth("100%");
		table.setCssClass("ui-xphd-bar");
		HoverButton sib = m_expandButton = new HoverButton(getImage(false), new IClicked<HoverButton>() {
			@Override
			public void clicked(@Nonnull HoverButton clickednode) throws Exception {
				toggleExpansion();
			}
		});
		TD td = body.addRowAndCell("ui-xphd-expandbutton");
		td.add(sib);
		td.setCellWidth("16");

		m_titleNode = td = body.addCell();
		NodeBase titleNode = getCaptionNode();
		if(null != titleNode)
			td.add(titleNode);
		else {
			String title = m_caption;
			if(null != title)
				td.add(title);
		}
		td.setCssClass("ui-xphd-ttl");
		td.setCellWidth("*");

		if(m_actionList.size() > 0) {
			td = body.addCell("ui-xphd-menubutton");
			td.setCellWidth("16");

			m_hamburgerButton = sib = new HoverButton("THEME/btnHeaderHamburger.png", new IClicked<HoverButton>() {
				@Override
				public void clicked(@Nonnull HoverButton clickednode) throws Exception {
					toggleMenu();
				}
			});
			td.add(sib);
		}
		add(m_content);

		if(m_expanded) {
			expand(true);
		}
	}

	private void toggleMenu() {
		HamburgerMenu menu = m_menu;
		if(null == menu || menu.isClosed()) {
			System.out.println("Rendering menu");
			m_menu = menu = new HamburgerMenu(m_actionList);
			nullChecked(m_hamburgerButton).appendAfterMe(menu);
			menu.setOnSelection(action -> {
				m_menu = null;
				action.execute(this, null);
			});
		} else {
			menu.remove();
			m_menu = null;
		}
	}

	/**
	 * If the hamburger menu has been opened close it.
	 */
	public void closeMenu() {
		HamburgerMenu menu = m_menu;
		if(null != menu) {
			m_menu = null;
			menu.remove();
		}
	}

	public void toggleExpansion() throws Exception {
		setExpanded(!m_expanded);
	}

	public void setExpanded(boolean expanded) throws Exception {
		if(m_expanded == expanded)
			return;
		expand(expanded);
	}

	private void expand(boolean expanded) throws Exception {
		m_expanded = expanded;

		HoverButton expandButton = m_expandButton;
		if(null == expandButton)						// Not built yet
			return;

		expandButton.setSrc(getImage(expanded));		// Switch icon

		//-- Render state
		if(expanded) {
			INotify<Div> onExpand = getOnExpand();
			if(null == onExpand) {
				m_content.add(new MessageLine(MsgType.ERROR, "The onExpand property, which tells me what to do when expand is pressed, is not set."));
			} else {
				onExpand.onNotify(m_content);
			}
			m_content.setDisplay(DisplayType.BLOCK);
		} else {
			m_content.removeAllChildren();
			m_content.setDisplay(DisplayType.NONE);
		}
	}

	public void setContent(@Nullable NodeBase node) {
		m_content.removeAllChildren();
		if(null != node)
			m_content.add(node);
	}

	@Nullable
	public String getCaption() {
		return m_caption;
	}

	public void setCaption(@Nullable String title) {
		m_caption = title;
		TD titleNode = m_titleNode;
		if(isBuilt() && titleNode != null) {
			titleNode.removeAllChildren();
			titleNode.add(title);
		}
	}

	public boolean isExpanded() {
		return m_expanded;
	}

	@Nullable
	public NodeBase getCaptionNode() {
		return m_captionNode;
	}

	public void setCaptionNode(@Nullable NodeBase captionNode) {
		m_captionNode = captionNode;
		TD titleNode = m_titleNode;
		if(isBuilt() && titleNode != null) {
			titleNode.removeAllChildren();
			if(null != captionNode)
				titleNode.add(captionNode);
		}
	}

	public List<IUIAction<?>> getActionList() {
		return m_actionList;
	}

	public void setActionList(List<IUIAction<?>> actionList) {
		forceRebuild();
		m_actionList = actionList;
	}

	/**
	 * Remove all actions from the action list.
	 */
	public void clearActions() {
		closeMenu();
		m_actionList.clear();
	}

	public void addAction(IUIAction<?> action) {
		m_actionList.add(action);
	}

	@Nullable
	public INotify<Div> getOnExpand() {
		return m_onExpand;
	}

	public void setOnExpand(@Nullable INotify<Div> onExpand) {
		m_onExpand = onExpand;
	}
}
