package to.etc.server.misc;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Created on Feb 4, 2005
 * @author jal
 */
public class HttpSessionWrapper implements HttpSession {
	private HttpSession	m_hs;

	public HttpSessionWrapper(HttpSession s) {
		m_hs = s;
	}

	/**
	 * @param arg0
	 * @return
	 */
	public Object getAttribute(String arg0) {
		return m_hs.getAttribute(arg0);
	}

	/**
	 * @return
	 */
	public Enumeration getAttributeNames() {
		return m_hs.getAttributeNames();
	}

	/**
	 * @return
	 */
	public long getCreationTime() {
		return m_hs.getCreationTime();
	}

	/**
	 * @return
	 */
	public String getId() {
		return m_hs.getId();
	}

	/**
	 * @return
	 */
	public long getLastAccessedTime() {
		return m_hs.getLastAccessedTime();
	}

	/**
	 * @return
	 */
	public int getMaxInactiveInterval() {
		return m_hs.getMaxInactiveInterval();
	}

	/**
	 * @return
	 */
	public ServletContext getServletContext() {
		return m_hs.getServletContext();
	}

	/**
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return m_hs.getSessionContext();
	}

	/**
	 * @param arg0
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public Object getValue(String arg0) {
		return m_hs.getValue(arg0);
	}

	/**
	 * @return
	 * @deprecated
	 */
	@Deprecated
	public String[] getValueNames() {
		return m_hs.getValueNames();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return m_hs.hashCode();
	}

	/**
	 * 
	 */
	public void invalidate() {
		m_hs.invalidate();
	}

	/**
	 * @return
	 */
	public boolean isNew() {
		return m_hs.isNew();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @deprecated
	 */
	@Deprecated
	public void putValue(String arg0, Object arg1) {
		m_hs.putValue(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public void removeAttribute(String arg0) {
		m_hs.removeAttribute(arg0);
	}

	/**
	 * @param arg0
	 * @deprecated
	 */
	@Deprecated
	public void removeValue(String arg0) {
		m_hs.removeValue(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_hs.setAttribute(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public void setMaxInactiveInterval(int arg0) {
		m_hs.setMaxInactiveInterval(arg0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_hs.toString();
	}
}
