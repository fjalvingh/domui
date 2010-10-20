package to.etc.domui.state;

import java.util.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Moving to other pages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2009
 */
final public class UIGoto {
	static public final String SINGLESHOT_MESSAGE = "uigoto.msgs";

	private UIGoto() {}

	static private WindowSession context() {
		return PageContext.getRequestContext().getWindowSession();
	}

	/**
	 * Destroy the current page and reload the exact same page with the same parameters as a
	 * new one. This has the effect of fully refreshing all data, and reinitializing the page
	 * at it's initial state.
	 */
	static public void reload() {
		Page pg = PageContext.getCurrentPage();
		Class< ? extends UrlPage> clz = pg.getBody().getClass();
		PageParameters pp = pg.getPageParameters();
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is parameterless, and is started in a NEW ConversationContext.
	 * @param clz
	 */
	static public void moveSub(final Class< ? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, null, null, null);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is started in a NEW ConversationContext.
	 *
	 * @param clz
	 * @param pp
	 */
	static public void moveSub(final Class< ? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, null, null, pp);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is started in a NEW ConversationContext.
	 *
	 * @param clz
	 * @param param	A list of parameters, in {@link PageParameters#addParameters(Object...)} format.
	 */
	static public void moveSub(final Class< ? extends UrlPage> clz, final Object... param) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		PageParameters pp;
		if(param == null || param.length == 0)
			pp = null;
		else
			pp = new PageParameters(param);
		context().internalSetNextPage(MoveMode.SUB, clz, null, null, pp);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page JOINS the conversation context passed; if the page does not accept
	 * that conversation an exception is thrown.
	 *
	 * @param clz
	 * @param cc
	 * @param pp
	 */
	static public void moveSub(final Class< ? extends UrlPage> clz, final ConversationContext cc, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		if(cc == null)
			throw new IllegalArgumentException("The conversation to join with cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, cc, null, pp);
	}

	/**
	 * Clear the entire shelf, then goto a new page. The page uses a NEW ConversationContext.
	 *
	 * @param clz
	 * @param pp
	 */
	static public void moveNew(final Class< ? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, pp);
	}

	/**
	 * Clear the entire shelf, then goto a new page. The page uses a NEW ConversationContext.
	 *
	 * @param clz
	 * @param param	A list of parameters, in {@link PageParameters#addParameters(Object...)} format.
	 */
	static public void moveNew(final Class< ? extends UrlPage> clz, Object... param) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		PageParameters pp;
		if(param == null || param.length == 0)
			pp = null;
		else
			pp = new PageParameters(param);
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, pp);
	}

	/**
	 * Clear the entire shelve, then goto a new page. The page uses a NEW ConversationContext.
	 * @param clz
	 */
	static public void moveNew(final Class< ? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, null);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * @param clz
	 */
	static public void replace(final Class< ? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, null);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * @param clz
	 * @param pp
	 */
	static public void replace(final Class< ? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * On the new page show the specified message as an UI message.
	 * @param pg
	 * @param clz
	 * @param pp
	 * @param msg
	 */
	static public final void replace(Page pg, final Class< ? extends UrlPage> clz, final PageParameters pp, UIMessage msg) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		List<UIMessage> msgl = new ArrayList<UIMessage>(1);
		msgl.add(msg);
		WindowSession ws = pg.getConversation().getWindowSession();
		ws.setAttribute(UIGoto.SINGLESHOT_MESSAGE, msgl);
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	static public void redirect(final String targeturl) {
		context().internalSetRedirect(targeturl);
	}

	/**
	 * Move to the previously-shelved page. That page is UNSHELVED and activated. If the shelve is
	 * EMPTY when this call is made the application moves back to the HOME page.
	 */
	static public void back() {
		context().internalSetNextPage(MoveMode.BACK, null, null, null, null);
	}

	/**
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message as an ERROR message.
	 *
	 * @param pg
	 * @param msg
	 */
	static public final void clearPageAndReload(Page pg, String msg) {
		clearPageAndReload(pg, pg.getBody().getClass(), pg.getPageParameters(), msg);
	}

	/**
	 * Destroy the current page and replace it with the new page specified with provided page parameters. On the new page show the specified
	 * message as an ERROR message.
	 *
	 * @param pg
	 * @param msg
	 * @param pp
	 */
	static public final void clearPageAndReload(Page pg, String msg, PageParameters pp) {
		clearPageAndReload(pg, pg.getBody().getClass(), pp, msg);
	}

	/**
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message as an ERROR message.
	 *
	 * @param pg
	 * @param target
	 * @param pp
	 * @param msg
	 */
	static public final void clearPageAndReload(Page pg, Class< ? extends UrlPage> target, PageParameters pp, String msg) {
		clearPageAndReload(pg, UIMessage.error(Msgs.BUNDLE, Msgs.S_PAGE_CLEARED, msg), pp);
	}

	/**
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message.
	 *
	 * @param pg
	 * @param msg
	 */
	static public final void clearPageAndReload(Page pg, UIMessage msg) {
		clearPageAndReload(pg, msg, pg.getPageParameters());
	}

	/**
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message.
	 *
	 * @param pg
	 * @param msg
	 * @param pp
	 */
	static public final void clearPageAndReload(Page pg, UIMessage msg, PageParameters pp) {
		WindowSession ws = pg.getConversation().getWindowSession();
		List<UIMessage> msgl = new ArrayList<UIMessage>(1);
		msgl.add(msg);
		ws.setAttribute(UIGoto.SINGLESHOT_MESSAGE, msgl);
		pg.getConversation().destroy();
		replace(pg.getBody().getClass(), pp);
	}
}
