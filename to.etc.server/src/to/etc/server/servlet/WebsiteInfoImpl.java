package to.etc.server.servlet;

import javax.servlet.http.*;

import to.etc.net.*;

public class WebsiteInfoImpl implements WebsiteInfo {
	/** The port number for this request (Cached) */
	private int		m_portnr;

	/** The original DNS hostname from the request. This does NOT include a port number. (Cached) */
	private String	m_hostname;

	private String	m_input_path;

	/** The saved URL to the host system, http://host:port/ only. (Cached) */
	private String	m_hosturl;

	private String	m_cached_siteurl;

	/** Output: the urlkey for subwebsites extracted from the request, */
	private String	m_urlkey;

	private String	m_rurl;

	public WebsiteInfoImpl() {
	}

	public WebsiteInfoImpl(HttpServletRequest req) {
		decode(req);
	}

	public void setRurl(String rurl) {
		m_rurl = rurl;
	}

	public String getRurl() {
		return m_rurl;
	}

	/**
	 * Called to actually decode the data.
	 * @param req
	 */
	public void decode(HttpServletRequest req) {
		//-- Initialize items to keep
		//		String sp = req.getServletPath();
		//		System.out.println("ServletPath="+sp);
		//		System.out.println("ContextPath="+req.getContextPath());

		m_hostname = NetTools.getHostName(req);
		m_portnr = NetTools.getHostPort(req);
		m_hosturl = NetTools.getHostURL(req);
		m_input_path = NetTools.getInputPath(req);

		//-- Get the context for this thingy,
		String cp = req.getContextPath();
		if(cp.startsWith("/"))
			cp = cp.substring(1);
		if(cp.length() > 0) {
			m_urlkey = cp;

			//-- Remove the context from the input URL.
			m_input_path = m_input_path.substring(cp.length() + 1);
		}
		//		System.out.println("Input URL="+m_input_path);
	}

	/**
	 * Returns the hostname as specified by the actual request. This overrides
	 * the HttpServletRequest's getServer call because that one lies about the
	 * actual host name for instance when running in Apache: all aliases are
	 * converted to the main sitename. This returns a host name without port 
	 * number.
	 * 
	 * @return	a host name (dns)
	 */
	public String getHostName() {
		return m_hostname;
	}

	public int getHostPort() {
		return m_portnr;
	}

	public String getHostURL() {
		return m_hosturl;
	}

	/**
	 * Returns the path part of the input URL, without host, port, 
	 * starting slash and parameters. The path has URLEncoded parts
	 * already replaced.
	 * 
	 * Example: for the url 
	 * <pre>http://www.mumble.to:8080/where/is/my?key=data</pre>
	 * this returns
	 * <pre>where/is/my</pre>
	 * 
	 * @return
	 */
	public String getInputPath() {
		return m_input_path;
	}

	/**
	 * This constructs the input URL for the current document. It takes the
	 * actual hostname and port name used, adds the protocol etc and adds the
	 * path info from the URL. The query string is NOT attached. This replaces
	 * getRequestURI() in HttpServletRequest because that lies about the
	 * server name :-(
	 * 
	 * @return	a string containing the full URL to the document, including the
	 * 			http: protocol indicator.
	 */
	public String getInputURL() {
		StringBuilder sb = new StringBuilder(128);
		sb.append(m_hosturl);
		sb.append(m_input_path);
		return sb.toString();
	}

	public String getContextName() {
		return m_urlkey;
	}

	/**
	 * Returns a complete URL to the ROOT of the current site. This is minimally
	 * the hostname and portnr followed by a site urlkey if present. If this
	 * is an admin provider the thing will contain the admin provider's URL.
	 * The URL returned ALWAYS ends in a /.
	 * @return
	 */
	public String getSiteURL() {
		if(m_cached_siteurl == null) {
			StringBuffer sb = new StringBuffer(64);
			sb.append(m_hosturl);
			if(m_urlkey != null) {
				sb.append(m_urlkey);
				sb.append('/');
			}
			m_cached_siteurl = sb.toString();
		}
		return m_cached_siteurl;
	}

	/**
	 * Get the path to the root of the current site without any host and protocol
	 * spec. This returns the URLKEY plus any admin provider strings. The thing
	 * never starts with a / and always ends with a /, except when this is a 
	 * host-only site; in that case this returns the empty string.
	 * @return
	 */
	public String getSiteRURL() {
		StringBuffer sb = new StringBuffer(32);
		if(m_urlkey != null) {
			sb.append(m_urlkey);
			sb.append('/');
		}
		return sb.toString();
	}
}
