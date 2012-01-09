package to.etc.xml;

import java.util.*;

public class MimeUtil {
	private final Map<String, String>	m_extraMap	= new HashMap<String, String>();

	/**
	 * The idiotic JSDK returns the entire content type header, including any subproperties (like charset) instead of
	 * decoding it as it bloody should. This separates the content mime type from any parameters. Real useful to do
	 * this for every bloody servlet.
	 *
	 * FIXME This needs a proper quote-handling subproperty decoder but I do not feel like building that again.
	 */
	public String parseHeader(final String mime) {
		m_extraMap.clear();
		if(mime == null)
			return null;
		int pos = mime.indexOf(';');
		if(pos == -1)
			return unquote(mime.trim());
		String resmime = unquote(mime.substring(0, pos).trim()).trim();
		String rest = mime.substring(pos + 1); // Any rest..
		String[] plist = rest.split(";");
		for(String pair : plist) {
			//-- Split into name=value
			pos = pair.indexOf('=');
			if(pos != -1) {
				String name = pair.substring(0, pos).trim();
				String value = pair.substring(pos + 1).trim();
				m_extraMap.put(name.toLowerCase(), unquote(value.trim()));
			}
		}
		return resmime;
	}

	static private String unquote(final String in) {
		String s = in.trim();
		int l = s.length();
		if(l < 2)
			return in;
		char a = s.charAt(0);
		char b = s.charAt(l - 1);
		if(a != b)
			return in;
		if(a == '\"' || a == '\'')
			return s.substring(1, l - 1);
		return in;
	}

	public String getExtra(final String name) {
		return m_extraMap.get(name.toLowerCase());
	}
}
