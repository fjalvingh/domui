package to.etc.domui.state;

import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-3-16.
 */
final public class ConversationDestroyedException extends CodeException {
	public ConversationDestroyedException(String id, String state) {
		super(Msgs.BUNDLE, Msgs.CONVERSATION_DESTROYED, id, state);
	}
}
