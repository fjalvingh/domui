package to.etc.dbutil;

import javax.annotation.*;

public class DbConnectionInfo {
	final private String m_hostname;

	final private String m_sid;

	final private String m_userid;

	final private String m_password;

	final private int m_port;

	private DbConnectionInfo(String hostname, String sid, String userid, String password, int port) {
		m_hostname = hostname;
		m_sid = sid;
		m_userid = userid;
		m_password = password;
		m_port = port;
	}

	/**
	 *
	 * @return
	 */
	@Nonnull
	static public DbConnectionInfo decode(@Nonnull String db) {
		int pos = db.indexOf('@');
		if(pos != -1) {
			String a = db.substring(0, pos);
			String b = db.substring(pos + 1);

			//-- Get userid/pw
			pos = a.indexOf(':');
			if(pos != -1) {
				String userid = a.substring(0, pos).trim();
				String password = a.substring(pos + 1).trim();

				pos = b.indexOf('/');
				if(pos != -1) {
					String sid = b.substring(pos + 1).trim();
					b = b.substring(0, pos);
					pos = b.indexOf(':');
					int port;
					String hostname;
					if(pos == -1) {
						port = 1521;
						hostname = b;
					} else {
						port = Integer.parseInt(b.substring(pos + 1).trim());
						hostname = b.substring(0, pos);
					}
					return new DbConnectionInfo(hostname, sid, userid, password, port);
				}
			}
		}
		throw new IllegalArgumentException("Invalid database connect string: must be 'user:password@host:port/SID', not " + db);
	}

	public String getHostname() {
		return m_hostname;
	}

	public String getSid() {
		return m_sid;
	}

	public String getUserid() {
		return m_userid;
	}

	public String getPassword() {
		return m_password;
	}

	public int getPort() {
		return m_port;
	}
}
