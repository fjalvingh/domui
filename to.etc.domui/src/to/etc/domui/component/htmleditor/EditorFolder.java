package to.etc.domui.component.htmleditor;

/**
 * Folder representation used by the editor's file browser.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 */
public class EditorFolder {
	private String m_name;

	private boolean m_hasChildren;

	private int m_acl = 255;

	public EditorFolder() {}

	public EditorFolder(String name, boolean hasChildren, int acl) {
		m_name = name;
		m_hasChildren = hasChildren;
		m_acl = acl;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public boolean isHasChildren() {
		return m_hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		m_hasChildren = hasChildren;
	}

	public int getAcl() {
		return m_acl;
	}

	public void setAcl(int acl) {
		m_acl = acl;
	}
}
