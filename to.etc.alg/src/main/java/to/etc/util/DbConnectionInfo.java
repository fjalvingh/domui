package to.etc.util;

import javax.annotation.*;

final public class DbConnectionInfo {
	private String	m_hostname;

	private String	m_sid;

	private String	m_userid;

	private String	m_password;

	private int		m_port;

	public DbConnectionInfo(String hostname, int port, String sid, String userid, String password) {
		m_hostname = hostname;
		m_port = port;
		m_sid = sid;
		m_userid = userid;
		m_password = password;
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
					if(pos != -1) {
						int port = Integer.parseInt(b.substring(pos + 1).trim());
						String hostname = b.substring(0, pos);
						return new DbConnectionInfo(hostname, port, sid, userid, password);
					} else {
						return new DbConnectionInfo(b, -1, sid, userid, password);
					}
				}
			}
		}
		throw new RuntimeException("Invalid database connect string: must be 'user:password@host:port/database', not " + db);
	}
}
