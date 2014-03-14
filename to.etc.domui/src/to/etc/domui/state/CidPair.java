package to.etc.domui.state;

import javax.annotation.*;
import javax.annotation.concurrent.*;

@Immutable
final public class CidPair {
	@Nonnull
	final private String m_windowId;

	@Nonnull
	final private String m_conversationId;

	public CidPair(@Nonnull String windowId, @Nonnull String conversationId) {
		m_windowId = windowId;
		m_conversationId = conversationId;
	}

	@Nonnull
	public String getWindowId() {
		return m_windowId;
	}

	@Nonnull
	public String getConversationId() {
		return m_conversationId;
	}

	@Nonnull
	static public CidPair decode(@Nonnull final String param) {
		if(param == null)
			throw new IllegalStateException("$cid cannot be null");
		int pos = param.indexOf('.');
		if(pos == -1)
			throw new IllegalStateException("Missing '.' in $CID parameter");
		return new CidPair(param.substring(0, pos), param.substring(pos + 1));
	}
}
