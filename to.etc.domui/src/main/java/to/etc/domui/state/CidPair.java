package to.etc.domui.state;

import jdk.nashorn.internal.ir.annotations.*;
import org.eclipse.jdt.annotation.*;

@Immutable
final public class CidPair {
	@NonNull
	final private String m_windowId;

	@NonNull
	final private String m_conversationId;

	public CidPair(@NonNull String windowId, @NonNull String conversationId) {
		m_windowId = windowId;
		m_conversationId = conversationId;
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
}
