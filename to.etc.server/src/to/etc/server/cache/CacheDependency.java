package to.etc.server.cache;

import java.io.*;

import to.etc.server.vfs.*;

/**
 * This stores all dependencies for a given cached resource. Dependencies are
 * stored as an Object and can be anything. The idea is that the stored objects
 * can be used to signal changes and clear all items cached using the resource.
 * In addition we can call changed() which checks, for every dependency that is
 * a VFS dependency, whether the associated resource has changed.
 *
 * @author jal
 * Created on Dec 11, 2005
 */
class CacheDependency implements Serializable {
	private static final long	serialVersionUID	= -4600705308612967142L;

	private long				m_timestamp;

	private Object				m_key;

	private long				m_ts_nextcheck;

	private int					m_checkInterval;

	CacheDependency(Object k, long ts, int checkInterval) {
		m_key = k;
		m_timestamp = ts;
		m_checkInterval = checkInterval;
	}

	//	CacheDependency(Object k, rence sref, long ts, int checkInterval)
	//	{
	//		m_key		= k;
	//		m_timestamp	= ts;
	//		m_sref		= sref;
	//		m_checkInterval	= checkInterval;
	//	}
	public final Object getKey() {
		return m_key;
	}

	private final VfsSource getSource() throws Exception {
		if(m_key instanceof VfsKey)
			return VFS.getInstance().get((VfsKey) m_key, null);
		return null;
	}

	public final long getTimestamp() {
		return m_timestamp;
	}

	public final boolean changed() {
		try {
			if(m_checkInterval > 0) {
				long ts = System.currentTimeMillis();
				synchronized(this) {
					if(ts < m_ts_nextcheck)
						return false;
					m_ts_nextcheck = ts + (1000l * m_checkInterval);
				}
			}
			VfsSource s = getSource();
			if(s == null)
				return m_timestamp != -1; // Return true if previous had a timestamp
			return m_timestamp != s.getDateModified();
		} catch(Exception x) {
			return true;
		}
	}
}
