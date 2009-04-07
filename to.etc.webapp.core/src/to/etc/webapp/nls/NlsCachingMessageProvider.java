package to.etc.webapp.nls;

import java.util.*;

/**
 * This is a wrapper for a message provider which caches all of the retrievals.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2006
 */
public class NlsCachingMessageProvider implements NlsMessageProvider {
	private NlsMessageProvider m_source;

	private Map<String, Map<Locale, String>> m_messageMap = new HashMap<String, Map<Locale, String>>();

	public NlsCachingMessageProvider(NlsMessageProvider source) {
		m_source = source;
	}

	public synchronized String findMessage(Locale loc, String code) {
		Map<Locale, String> m = m_messageMap.get(code);
		if(m == null) {
			m = new HashMap<Locale, String>();
			m_messageMap.put(code, m);
		}
		String msg = m.get(loc);
		if(msg != null)
			return msg;

		msg = m_source.findMessage(loc, code);
		if(msg != null)
			m.put(loc, msg);
		return msg;
	}

}
