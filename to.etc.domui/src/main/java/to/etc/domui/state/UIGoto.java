/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.SpiPage;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.spi.SpiContainer;
import to.etc.domui.util.ISpiContainerName;
import to.etc.domui.util.Msgs;

import java.util.ArrayList;
import java.util.List;

/**
 * Moving to other pages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 9, 2009
 */
final public class UIGoto {
	@Deprecated
	static public final String SINGLESHOT_MESSAGE = "uigoto.msgs";

	static public final String PAGE_ACTION = "uigoto.action";

	private UIGoto() {
	}

	static private WindowSession context() {
		return UIContext.getRequestContext().getWindowSession();
	}

	/**
	 * Destroy the current page and reload the exact same page with the same parameters as a
	 * new one. This has the effect of fully refreshing all data, and reinitializing the page
	 * at it's initial state.
	 */
	static public void reload() {
		Page pg = UIContext.getCurrentPage();
		Class<? extends UrlPage> clz = pg.getBody().getClass();
		IPageParameters pp = pg.getPageParameters();
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	/**
	 * Destroy the current page, and reload a fresh copy with fresh new parameters.
	 */
	static public void reload(@NonNull IPageParameters pp) {
		Page pg = UIContext.getCurrentPage();
		Class<? extends UrlPage> clz = pg.getBody().getClass();
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	static public void reload(@NonNull Object... parameters) {
		PageParameters pp = new PageParameters(parameters);
		reload(pp);
	}

	/**
	 * Add a "goto action" to be executed on the page we will go-to.
	 */
	static public void addAction(@NonNull IGotoAction action) {
		WindowSession ws = UIContext.getCurrentConversation().getWindowSession();
		List<IGotoAction> ga = (List<IGotoAction>) ws.getAttribute(PAGE_ACTION);
		if(null == ga) {
			ga = new ArrayList<IGotoAction>();
			ws.setAttribute(PAGE_ACTION, ga);
		}
		ga.add(action);
	}

	/**
	 * Add a message as a {@link IGotoAction} action. It will be shown as a {@link MessageFlare}.
	 */
	static public void addActionMessage(@NonNull final UIMessage message) {
		addAction(new IGotoAction() {
			@Override
			public void executeAction(@NonNull UrlPage page) throws Exception {
				MessageFlare.display(page, message);
			}
		});
	}

	/**
	 * Add a message as a {@link IGotoAction} action. It will be shown as a {@link MessageFlare}.
	 */
	static public void addActionMessage(@NonNull final MsgType type, @NonNull final String message) {
		addAction(new IGotoAction() {
			@Override
			public void executeAction(@NonNull UrlPage page) throws Exception {
				MessageFlare.display(page, type, message);
			}
		});
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is parameterless, and is started in a NEW ConversationContext.
	 */
	static public void moveSub(final Class<? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, null, null, null);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is started in a NEW ConversationContext.
	 */
	static public void moveSub(final Class<? extends UrlPage> clz, final IPageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, null, null, pp);
	}

	/**
	 * Push (shelve) the current page, then move to a new page. The page is started in a NEW ConversationContext.
	 */
	static public void moveSub(final Class<? extends UrlPage> clz, final Object... param) {
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
	 */
	static public void moveSub(final Class<? extends UrlPage> clz, final ConversationContext cc, final IPageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		if(cc == null)
			throw new IllegalArgumentException("The conversation to join with cannot be null");
		context().internalSetNextPage(MoveMode.SUB, clz, cc, null, pp);
	}

	/**
	 * Clear the entire shelf, then goto a new page. The page uses a NEW ConversationContext.
	 */
	static public void moveNew(final Class<? extends UrlPage> clz, final IPageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, pp);
	}

	/**
	 * Clear the entire shelf, then goto a new page. The page uses a NEW ConversationContext.
	 */
	static public void moveNew(final Class<? extends UrlPage> clz, Object... param) {
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
	 */
	static public void moveNew(final Class<? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.NEW, clz, null, null, null);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 */
	static public void replace(final Class<? extends UrlPage> clz) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, null);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 */
	static public void replace(final Class<? extends UrlPage> clz, final IPageParameters pp) {
		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 */
	static public void replace(Class< ? extends UrlPage> clz, Object... param) {
		PageParameters pp;
		if(param == null || param.length == 0)
			pp = null;
		else
			pp = new PageParameters(param);

		if(clz == null)
			throw new IllegalArgumentException("The class to move-to cannot be null");
		context().internalSetNextPage(MoveMode.REPLACE, clz, null, null, pp);
	}

	/**
	 * Replace the "current" page with a new page. The current page is destroyed; the shelve stack is not changed.
	 * On the new page show the specified message as an UI message.
	 */
	static public final void replace(Page pg, final Class<? extends UrlPage> clz, final IPageParameters pp, UIMessage msg) {
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
	 * Deprecated - use {@link #reload()} or one of it's variants instead.
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message as an ERROR message.
	 */
	@Deprecated
	static public final void clearPageAndReload(Page pg, String msg) {
		clearPageAndReload(pg, msg, pg.getPageParameters());
	}

	/**
	 * Deprecated - use {@link #reload()} or one of it's variants instead.
	 * Destroy the current page and replace it with the new page specified with provided page parameters. On the new page show the specified
	 * message as an ERROR message.
	 */
	@Deprecated
	static public final void clearPageAndReload(Page pg, String msg, IPageParameters pp) {
		clearPageAndReload(pg, UIMessage.info(Msgs.BUNDLE, Msgs.S_PAGE_CLEARED, msg), pp);
	}

	/**
	 * Deprecated - use {@link #reload()} or one of it's variants instead.
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message.
	 */
	@Deprecated
	static public final void clearPageAndReload(Page pg, UIMessage msg) {
		clearPageAndReload(pg, msg, pg.getPageParameters());
	}

	/**
	 * Deprecated - use {@link #reload()} or one of it's variants instead.
	 * Destroy the current page and replace it with the new page specified. On the new page show the specified
	 * message.
	 */
	@Deprecated
	static public final void clearPageAndReload(Page pg, UIMessage msg, IPageParameters pp) {
		WindowSession ws = pg.getConversation().getWindowSession();
		List<UIMessage> msgl = new ArrayList<UIMessage>(1);
		msgl.add(msg);
		ws.setAttribute(UIGoto.SINGLESHOT_MESSAGE, msgl);

		/*
		 * jal 20120604 Do NOT add "destroyConversation" here- replacing the page will properly destroy the context. Destroying
		 * it twice will cause the history stack to become corrupt as two pages will be deleted.
		 */
		replace(pg.getBody().getClass(), pp); // Destroy the current page, and replace with a new one. This will also destroy
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	SPI interface calls.										*/
	/*----------------------------------------------------------------------*/

	/**
	 * Move to a new page, and put the previous one on the stack. If the "new page" is actually stacked already
	 * the stack is rewound back.
	 */
	static public void moveSub(@NonNull ISpiContainerName name, @NonNull Class<? extends SubPage> spiPage, Object... param) throws Exception {
		PageParameters pp;
		if(param == null)
			pp = new PageParameters();
		else
			pp = new PageParameters(param);
		moveSub(name, spiPage, pp);
	}

	/**
	 * Move to a new page, and put the previous one on the stack. If the "new page" is actually stacked already
	 * the stack is rewound back.
	 */
	static public void moveSub(@NonNull ISpiContainerName name, @NonNull Class<? extends SubPage> spiClass, @NonNull IPageParameters pp) throws Exception {
		SpiContainer container = getSpiContainer(name);
		container.handleMoveSub(spiClass, pp);
	}

	@NonNull
	private static SpiContainer getSpiContainer(@NonNull ISpiContainerName name) {
		Page currentPage = UIContext.getCurrentPage();
		UrlPage body = currentPage.getBody();
		if(!(body instanceof SpiPage)) {
			throw new IllegalStateException("The current page is not a SpiPage");
		}
		SpiPage spiPage = (SpiPage) body;
		SpiContainer container = spiPage.findSpiContainerByName(name.name());
		if(null == container)
			throw new IllegalArgumentException("SPI Page " + body.getClass().getName() + " does not have a container named " + name.name());
		return container;
	}

	/**
	 * Replace the current topmost page in the container with a new page. The old page gets destroyed, so
	 * the stack size remains the same. If the page to replace is already on the stack then the stack
	 * is unwound till that page before the replace takes place; in that case the "old" entry on the stack
	 * will also be destroyed and be replaced with a new fresh page.
	 */
	static public void replace(@NonNull ISpiContainerName name, @NonNull Class<? extends SubPage> spiClass, @Nullable IPageParameters pp) throws Exception {
		SpiContainer container = getSpiContainer(name);
		container.replace(spiClass, pp);
	}

	/**
	 * Clear the entire shelf, and start completely anew with a fresh page on top.
	 */
	static public void moveNew(@NonNull ISpiContainerName name, @NonNull Class<? extends SubPage> spiClass, @Nullable IPageParameters pp) throws Exception {
		SpiContainer container = getSpiContainer(name);
		container.moveNew(spiClass, pp);
	}

	static public void moveNew(@NonNull ISpiContainerName name, @NonNull Class<? extends SubPage> spiPage, Object... param) throws Exception {
		PageParameters pp;
		if(param == null)
			pp = new PageParameters();
		else
			pp = new PageParameters(param);

		moveNew(name, spiPage, pp);
	}
}
