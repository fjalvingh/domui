package to.etc.domui.dom.errors;

import to.etc.domui.dom.html.*;

/**
 * The fence over which errors cannot pass. An error fence maintains the
 * list of listerers that are interested in an error.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 29, 2008
 */
public interface IErrorFence {
	public void addErrorListener(IErrorMessageListener eml);

	public void removeErrorListener(IErrorMessageListener eml);

	public void addMessage(NodeBase source, UIMessage uim);

	public void removeMessage(NodeBase source, UIMessage uim);

	public void clearGlobalMessages(NodeBase source, String code);
}
