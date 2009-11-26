package to.etc.domui.dom.errors;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Use this error fence handler in case when some UIMessage should be handled by more than one error fence.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 23 Sep 2009
 */
public class PropagatingErrorFenceHandler extends ErrorFenceHandler {

	public PropagatingErrorFenceHandler(NodeContainer container) {
		super(container);
	}

	@Override
	public void addMessage(NodeBase source, UIMessage uim) {
		super.addMessage(source, uim);
		NodeContainer propagationContainer = (getContainer() != null && getContainer().getParent() != null) ? getContainer().getParent() : null;
		if(propagationContainer != null) {
			IErrorFence fence = DomUtil.getMessageFence(propagationContainer);
			if(fence != this) {
				fence.addMessage(source, uim);
			}
		}
	}

	@Override
	public void removeMessage(NodeBase source, UIMessage uim) {
		super.removeMessage(source, uim);
		NodeContainer propagationContainer = (getContainer() != null && getContainer().getParent() != null) ? getContainer().getParent() : null;
		if(propagationContainer != null) {
			IErrorFence fence = DomUtil.getMessageFence(propagationContainer);
			if(fence != this) {
				fence.removeMessage(source, uim);
			}
		}
	}

}
