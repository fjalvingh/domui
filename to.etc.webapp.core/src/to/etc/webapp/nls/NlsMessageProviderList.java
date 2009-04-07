package to.etc.webapp.nls;

import java.util.*;

/**
 * A list of message providers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2006
 */
public class NlsMessageProviderList implements NlsMessageProvider {
	/** The list of all message bundles that must be searched when rendering a global message. */
	private List<NlsMessageProvider> m_providerList = new ArrayList<NlsMessageProvider>();

	public String findMessage(Locale loc, String code) {
		for(NlsMessageProvider p : m_providerList) {
			String msg = p.findMessage(loc, code);
			if(msg != null)
				return msg;
		}
		return null;
	}

	public void addProvider(NlsMessageProvider p) {
		m_providerList.add(p);
	}
}
