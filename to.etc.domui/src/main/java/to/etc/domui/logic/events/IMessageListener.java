package to.etc.domui.logic.events;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.errors.UIMessage;

import java.util.List;

/**
 * Called when some kind of message is added.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 12, 2013
 */
public interface IMessageListener {
	void actionMessages(@NonNull List<UIMessage> msgl);
}
