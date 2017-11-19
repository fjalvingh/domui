package to.etc.domui.logic.events;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;

/**
 * Called when some kind of message is added.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 12, 2013
 */
public interface IMessageListener {
	void actionMessages(@Nonnull List<UIMessage> msgl);
}
