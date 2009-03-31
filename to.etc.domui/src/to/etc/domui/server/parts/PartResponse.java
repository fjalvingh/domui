package to.etc.domui.server.parts;

import java.io.*;

/**
 * Describes the response for a part.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 13, 2009
 */
public class PartResponse {
	private OutputStream		m_os;
	private String				m_mime;
	private int					m_cacheTime;

	public PartResponse(OutputStream os) {
		m_os = os;
	}
	public OutputStream			getOutputStream() {
		return m_os;
	}
	public String getMime() {
		return m_mime;
	}
	public void setMime(String mime) {
		m_mime = mime;
	}
	/**
	 * The time the response may be cached by the browser without inquiry, in seconds.
	 * @return
	 */
	public int getCacheTime() {
		return m_cacheTime;
	}
	/**
	 * Set the time the response may be cached by the browser without inquiry, in seconds.
	 * @param cacheTime
	 */
	public void setCacheTime(int cacheTime) {
		m_cacheTime = cacheTime;
	}
}
