package to.etc.domui.component.delayed;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

/**
 * This is a div whose content can be refreshed automatically because the client polls
 * for changes every few seconds. The content for the div must be updated for every
 * call to checkForChanges().
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2009
 */
public class PollingDiv extends Div implements IPolledForUpdate {
	/**
	 * This is the method which gets called every few seconds. The default implementation
	 * just forces a rebuild on this component causing it to be rebuilt anew fully. Better
	 * implementations only change what's really needed to prevent excessive screen updates
	 * and flickering browser horror.
	 * @throws Exception
	 */
	@Override
	public void checkForChanges() throws Exception {
		forceRebuild();
		build();
	}

	/**
	 * Register as a poll thing when added to page.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().getConversation().registerPoller(this);
	}

	/**
	 * Unregister as poller when removed.
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		getPage().getConversation().unregisterPoller(this);
	}
}
