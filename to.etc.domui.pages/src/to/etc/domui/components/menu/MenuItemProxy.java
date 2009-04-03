package to.etc.domui.components.menu;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.utils.*;

/**
 * Proxy to an actual MenuItem which can be used to override a menu with a different order, tree structure.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItemProxy implements IMenuItem {
	/** The actual item being proxied. */
	private final IMenuItem			m_actual;
	private List<IMenuItem>			m_children;
	private int						m_order;
	private IMenuItem				m_parent;

	public MenuItemProxy(final IMenuItem actual) {
		if(null == actual)
			throw new NullPointerException("?? Null MenuItem here?");
		m_actual = actual;
		if(m_actual.isSubMenu())
			m_children = new ArrayList<IMenuItem>();
	}

	public String getDescription() {
		return m_actual.getDescription();
	}

	public String getIconPath() {
		return m_actual.getIconPath();
	}

	public String getId() {
		return m_actual.getId();
	}

	public String getLabel() {
		return m_actual.getLabel();
	}

	public Class< ? extends UrlPage> getPageClass() {
		return m_actual.getPageClass();
	}

	public PageParameters getPageParameters() {
		return m_actual.getPageParameters();
	}

	public String getParentID() {
		return m_actual.getParentID();
	}

	public Right[] getRequiredRights() {
		return m_actual.getRequiredRights();
	}

	public String getSearchString() {
		return m_actual.getSearchString();
	}

	public boolean isDisabled() {
		return m_actual.isDisabled();
	}
	public boolean isSubMenu() {
		return m_actual.isSubMenu();
	}

	public List<IMenuItem> getChildren() {
		return m_children;
	}
	public void setChildren(final List<IMenuItem> children) {
		m_children = children;
	}
	public int getOrder() {
		return m_order;
	}

	public IMenuItem getParent() {
		return m_parent;
	}
	public void setParent(final IMenuItem parent) {
		m_parent = parent;
	}
}
