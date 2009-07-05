package to.etc.domui.component.htmleditor;

import java.util.*;

public class EditorResourceType {
	private String m_name;

	private String m_rootURL;

	private Set<String> m_allowedExtensions;

	private Set<String> m_deniedExtensions;

	private int m_acl = 255;

	public EditorResourceType() {}

	public EditorResourceType(String name, String rootURL, int acl, Set<String> allowedExtensions, Set<String> deniedExtensions) {
		m_name = name;
		m_rootURL = rootURL;
		m_acl = acl;
		m_allowedExtensions = allowedExtensions;
		m_deniedExtensions = deniedExtensions;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getRootURL() {
		return m_rootURL;
	}

	public void setRootURL(String rootURL) {
		m_rootURL = rootURL;
	}

	public Set<String> getAllowedExtensions() {
		return m_allowedExtensions;
	}

	public void setAllowedExtensions(Set<String> allowedExtensions) {
		m_allowedExtensions = allowedExtensions;
	}

	public Set<String> getDeniedExtensions() {
		return m_deniedExtensions;
	}

	public void setDeniedExtensions(Set<String> deniedExtensions) {
		m_deniedExtensions = deniedExtensions;
	}

	public int getAcl() {
		return m_acl;
	}

	public void setAcl(int acl) {
		m_acl = acl;
	}
}
