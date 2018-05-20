/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.server;


/**
 * Decoded version code for the user-agent string.
 * Agent strings:
 * <ul>
 * 	<li>Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)</li>
 * 	<li>Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.14) Gecko/2009090217 Ubuntu/9.04 (jaunty) Firefox/3.0.14</li>
 *	<li>Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)</li>
 *	<li>Mozilla/5.0 (compatible; Konqueror/3.5; Linux; en_US) KHTML/3.5.10 (like Gecko) (Debian)</li>
 *	<li>Mozilla/4.7 [en] (WinNT; U)</li>
 *	<li>
 *	<li>Opera/9.64 (X11; Linux x86_64; U; nl) Presto/2.1.1</li>
 * </ul>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public class BrowserVersion {
	static public final BrowserVersion INSTANCE = parseUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");

	/** The formal name of the browser (MSIE, Firefox, Opera, Safari) */
	private String m_browserName;

	private String m_browserVersion;

	private int[] m_version;

	//	private String m_os;

	static public BrowserVersion parseUserAgent(String ua) {
		BrowserVersion bv = new BrowserVersion();
		bv.parse(ua);
		return bv;
	}

	private void parse(String ua) {
		if(ua.contains("Trident/7.0")) {
			handleIE11();
			return;
		}
	
		if(ua.startsWith("Mozilla/")) {
			decodeMozilla(ua);
			return;
		}

		//-- Browser name and version is 1st token
		int pos = ua.indexOf('/');
		if(pos == -1)
			return;
		m_browserName = ua.substring(0, pos).trim();		// Opera, Lynx
		int sp = ua.indexOf(' ', pos);
		if(sp == -1)
			m_browserVersion = ua.substring(pos + 1);
		else
			m_browserVersion = ua.substring(pos + 1, sp).trim();
	}

	private void handleIE11() {
		m_browserName = "MSIE";
		m_browserVersion = "11.0";
	}

	private void decodeMozilla(String ua) {
		//-- Find compatible; if present browser follows;
		int pos = ua.indexOf("compatible");
		if(pos != -1) {
			int p1 = ua.indexOf(';', pos + 10);
			if(p1 != -1) {
				int p2 = ua.indexOf(';', p1 + 1);
				if(p2 != -1) {
					String br = ua.substring(p1 + 1, p2).trim();
					decodeBrowser(br);
					return;
				}
			}
		}

		//-- Failed- try last fragment
		pos = ua.lastIndexOf(' ');
		if(pos != -1) {
			decodeBrowser(ua.substring(pos + 1));
		}
	}

	private void decodeBrowser(String br) {
		int pos = br.indexOf(' ');
		if(pos == -1) {
			pos = br.indexOf('/');
			if(pos == -1) {
				m_browserName = br;
				return;
			}
		}
		m_browserName = br.substring(0, pos);
		m_browserVersion = br.substring(pos + 1).trim();
	}

	public String getBrowserName() {
		return m_browserName;
	}

	public String getBrowserVersion() {
		return m_browserVersion;
	}

	private int[] getVer() {
		if(m_version == null) {
			if(m_browserVersion == null || m_browserVersion.length() == 0) {
				m_version = new int[]{0, 0};
				return m_version;
			}
			String[] ar = m_browserVersion.split("\\.");
			if(ar.length == 0)
				ar = new String[]{m_browserVersion};
			m_version = new int[ar.length];
			for(int i = 0; i < ar.length; i++) {
				try {
					m_version[i] = Integer.parseInt(ar[i]);
				} catch(Exception x) {}
			}
		}
		return m_version;
	}

	private int getVer(int ix) {
		int[] v = getVer();
		if(ix >= v.length)
			return 0;
		return v[ix];
	}

	public int getMajorVersion() {
		return getVer(0);
	}

	public int getMinorVersion() {
		return getVer(1);
	}

	private static void check(String br, String bv, String ua) {
		BrowserVersion v = BrowserVersion.parseUserAgent(ua);
		if(!br.equals(v.getBrowserName())) {
			System.out.println("FAIL: Expected " + br + " got " + v.getBrowserName() + " in ua=" + ua);
		}
		if(!bv.equals(v.getBrowserVersion())) {
			System.out.println("FAIL: Expected " + bv + " got " + v.getBrowserVersion() + " in ua=" + ua);
		}
	}

	public boolean isIE() {
		return "MSIE".equals(getBrowserName());
	}

	public boolean isOpera() {
		return "Opera".equals(getBrowserName());
	}

	public boolean isKonqueror() {
		return "Konqueror".equals(getBrowserName());
	}

	public boolean isFirefox() {
		return "Firefox".equals(getBrowserName());
	}

	public boolean isMozilla() {
		return "Mozilla".equals(getBrowserName());
	}

	public static void main(String[] args) {
		try {
			check("Opera", "9.64", "Opera/9.64 (X11; Linux x86_64; U; nl) Presto/2.1.1");
			check("MSIE", "7.0", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");
			check("Firefox", "3.0.14", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.14) Gecko/2009090217 Ubuntu/9.04 (jaunty) Firefox/3.0.14");
			check("MSIE", "8.0", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
			check("Konqueror", "3.5", "Mozilla/5.0 (compatible; Konqueror/3.5; Linux; en_US) KHTML/3.5.10 (like Gecko) (Debian)");
			//			check("", "", "");

		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "[browser " + m_browserName + ", version=" + m_browserVersion + "]";
	}

}
