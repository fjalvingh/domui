package to.etc.webapp.eventmanager;

import java.io.*;
import java.util.*;

/**
 * Base class for all ViewPoint application events, as handled
 * by the event manager.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 12, 2006
 */
public class AppEventBase implements Serializable {
	private static final long serialVersionUID = 1L;

	private long m_upid;

	private String m_server;

	private Date m_timestamp;

	public String getServer() {
		return m_server;
	}

	void setServer(final String server) {
		m_server = server;
	}

	public Date getTimestamp() {
		return m_timestamp;
	}

	void setTimestamp(final Date timestamp) {
		m_timestamp = timestamp;
	}

	public long getUpid() {
		return m_upid;
	}

	void setUpid(final long upid) {
		m_upid = upid;
	}

	@Override
	public String toString() {
		return "AppEvent[" + m_upid + "@" + m_server + "/" + getClass().getName() + "]";
	}
}
