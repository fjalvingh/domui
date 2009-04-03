package to.etc.domui.components.menu;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.nls.*;
import to.etc.domui.utils.*;

/**
 * A single item in the menu, as defined by the *code*.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2009
 */
public class MenuItem {
	private String m_id;

	private BundleRef m_msgBundle;

	private String m_labelKey;

	private String m_descKey;

	private String m_searchKey;

	private String m_parentID;

	private Class< ? extends UrlPage> m_pageClass;

	private PageParameters m_pageParameters;

	private String m_iconPath;

	private boolean m_disabled;

	/** The list of rights the user MUST have to access this menu item. This can get delegated to the UrlPage's annotation. */
	private Right[]		m_requiredRights;

	private final List<MenuItem> m_children = new ArrayList<MenuItem>();

	public MenuItem		setAction(final Class<? extends UrlPage> clz) {
		m_pageClass = clz;
		return this;
	}

	/**
	 * Defines the rights that the user MUST have to see this menu item. If more than one Right is passed the user needs to possess *all* rights. When
	 * defined this overrides the rights in
	 * @param name
	 * @return
	 */
	public MenuItem		setRequiredRights(final Right... rights) {
		m_requiredRights = rights;
		return this;
	}
	public MenuItem		setImage(final String name) {
		m_iconPath = name;
		return this;
	}
	public MenuItem		setImage(final Class<?> res, final String name) {
		m_iconPath = DomUtil.getJavaResourceRURL(res, name);
		return this;
	}

	private String	byKey(final String k) {
		Locale loc = NlsContext.getLocale();
		return m_msgBundle.getString(loc, k);
	}

	public String getId() {
		return m_id;
	}
	public BundleRef getMsgBundle() {
		return m_msgBundle;
	}
	public String getLabelKey() {
		return m_labelKey;
	}
	public String getDescKey() {
		return m_descKey;
	}
	public String getSearchKey() {
		return m_searchKey;
	}
	public String getParentID() {
		return m_parentID;
	}
	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}
	public PageParameters getPageParameters() {
		return m_pageParameters;
	}
	public String getIconPath() {
		return m_iconPath;
	}
	public boolean isDisabled() {
		return m_disabled;
	}
	public List<MenuItem> getChildren() {
		return m_children;
	}
    public String getSearchString() {
        return byKey(m_searchKey);
    }
    public Right[] getRequiredRights() {
		return m_requiredRights;
	}
    public String	getLabel() {
    	return byKey(m_labelKey);
    }
    public String	getDescription() {
    	return byKey(m_descKey);
    }
}
