package to.etc.domui.server;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

/**
 * EXPERIMENTAL INTERFACE DomUI state change listener.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
public interface IDomUIStateListener {
	void windowSessionCreated(WindowSession ws) throws Exception;

	void windowSessionDestroyed(WindowSession ws) throws Exception;

	void conversationCreated(ConversationContext cc) throws Exception;

	void conversationDestroyed(ConversationContext cc) throws Exception;

//	void pageCreated(Page pg) throws Exception;
//
//	void pageDestroyed(Page pg) throws Exception;

	/**
	 * Called just before the page is rendered fully.
	 * @param ctx
	 */
	void onBeforeFullRender(RequestContextImpl ctx, Page pg);

	/**
	 * Called just before page actions are executed (AJAX requests)
	 * @param ctx
	 * @param pg
	 */
	void onBeforePageAction(RequestContextImpl ctx, Page pg);

	void onAfterPage(IRequestContext ctx, Page pg);
}
