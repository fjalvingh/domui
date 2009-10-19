package to.etc.domui.server;

/**
 * Decoded version code for the user-agent string.
 * Agent strings:
 * <ul>
 * 	<li>Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)</li>
 * 	<li>Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.14) Gecko/2009090217 Ubuntu/9.04 (jaunty) Firefox/3.0.14</li>
 *	<li>Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)</li>
 *	<li>Mozilla/5.0 (compatible; Konqueror/3.5; Linux; en_US) KHTML/3.5.10 (like Gecko) (Debian)</li>
 *	<li>Opera/9.64 (X11; Linux x86_64; U; nl) Presto/2.1.1</li>
 * </ul>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public class BrowserVersion {
	private String m_agentString;

	/** The formal name of the browser (MSIE, Firefox, Opera, Safari) */
	private String m_browserName;

	private int m_majorVersion, m_minorVersion;

	private String m_os;

	static public BrowserVersion parseUserAgent(String ua) {
		BrowserVersion bv = new BrowserVersion();
		bv.parse(ua);
		return bv;
	}

	private void parse(String ua) {
		if(ua == null)
			return;
		m_agentString = ua = ua.trim();
		if(ua.length() == 0)
			return;

		//-- Start parsing: first part before space is initial browser/version
		int pos = ua.indexOf(' ');
		if(pos == -1)
			return;
		String hd = ua.substring(0, pos).trim(); // Should contain 'Mozilla/5.0' alike string
		if(hd.length() == 0)
			return;
		decodeInitialFragment(ua);
	}




}
