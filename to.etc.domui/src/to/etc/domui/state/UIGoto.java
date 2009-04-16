package to.etc.domui.state;

import to.etc.domui.dom.html.*;

/**
 * Moving to other pages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2009
 */
final public class UIGoto {
	private UIGoto() {
	}

	static private WindowSession	context() {
		return PageContext.getRequestContext().getWindowSession();
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is parameterless, and is started in a NEW ConversationContext.
	 * @param clz
	 */
	static public void 	moveSub(final Class<? extends UrlPage> clz) {
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
	static public void	moveSub(final Class<? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
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
	static public void	moveSub(final Class<? extends UrlPage> clz, final ConversationContext cc, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		if(cc == null)
			throw new IllegalArgumentException("The conversation to join with cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, cc, null, pp);
	}

	/**
	 * Clear the entire shelve, then goto a new page. The page uses a NEW ConversationContext.
	 *
	 * @param clz
	 * @param pp
	 */
	static public void	moveNew(final Class<? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, pp);
	}

	/**
	 * Clear the entire shelve, then goto a new page. The page uses a NEW ConversationContext.
	 * @param clz
	 */
	static public void 	moveNew(final Class<? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, null);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * @param clz
	 */
	static public void	replace(final Class<? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, null);
	}
	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * @param clz
	 * @param pp
	 */
	static public void	replace(final Class<? extends UrlPage> clz, final PageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	static public void	redirect(final String targeturl) {
		context().internalSetRedirect(targeturl);
	}

	/**
	 * Move to the previously-shelved page. That page is UNSHELVED and activated. If the shelve is
	 * EMPTY when this call is made the application moves back to the HOME page.
	 */
	static public void	back() {
		context().internalSetNextPage(MoveMode.BACK, null, null, null, null);
	}
}
