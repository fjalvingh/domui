package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class CidPair {
	static private final Logger LOG = LoggerFactory.getLogger(CidPair.class);

	@NonNull
	final private String m_windowId;

	@NonNull
	final private String m_conversationId;

	public CidPair(@NonNull String windowId, @NonNull String conversationId) {
		m_windowId = windowId;
		m_conversationId = conversationId;

		if(!isValid(windowId)) {
			LOG.error("Invalid window ID in $CID " + windowId + "." + conversationId);
			throw new IllegalStateException("Invalid window ID in CID");    // Do not show the ID - can be a XSS attack
		}
		if(!isValid(conversationId)) {
			LOG.error("Invalid conversation ID in $CID " + windowId + "." + conversationId);
			throw new IllegalStateException("Invalid conversation ID in CID");    // Do not show the ID - can be a XSS attack
		}
	}

	private boolean isValid(String thing) {
		for(int i = thing.length(); --i >= 0; ) {
			char c = thing.charAt(i);
			if(c != '_' && !Character.isLetterOrDigit(c))
				return false;
		}
		return true;
	}

	@NonNull
	public String getWindowId() {
		return m_windowId;
	}

	@NonNull
	public String getConversationId() {
		return m_conversationId;
	}

	@NonNull
	static public CidPair decode(@NonNull final String param) {
		if(param == null)
			throw new IllegalStateException("$cid cannot be null");
		int pos = param.indexOf('.');
		if(pos == -1)
			throw new IllegalStateException("Missing '.' in $CID parameter");
		return new CidPair(param.substring(0, pos), param.substring(pos + 1));
	}

	@Nullable
	static public CidPair decodeLax(@NonNull final String param) {
		if(param == null)
			return null;
		int pos = param.indexOf('.');
		if(pos == -1)
			return null;
		return new CidPair(param.substring(0, pos), param.substring(pos + 1));
	}

	@Override
	public String toString() {
		return m_windowId + "." + m_conversationId;
	}
}
