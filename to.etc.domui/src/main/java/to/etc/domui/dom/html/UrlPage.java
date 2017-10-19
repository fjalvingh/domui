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
package to.etc.domui.dom.html;

import to.etc.domui.component.layout.BreadCrumb;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.layout.title.AppPageTitleBar;
import to.etc.domui.logic.ILogicContext;
import to.etc.domui.logic.LogicContextImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.Constants;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * The base for all pages that can be accessed through URL's. This is mostly a
 * dummy class which ensures that all pages/fragments properly extend from DIV,
 * ensuring that the Page logic can replace the "div" tag with a "body" tag for
 * root fragments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 1, 2008
 */
@DefaultNonNull
public class UrlPage extends Div {
	/** The title for the page in the head's TITLE tag. */
	@Nullable
	private String m_pageTitle;

	@Nullable
	private INotifyPageEvent m_notifyPageEvent;

	private IThemeVariant m_themeVariant = DefaultThemeVariant.INSTANCE;

	public UrlPage() {
	}

	/**
	 * Set the style of the theme to use for the entire page. The normal style is "default", represented
	 * by {@link DefaultThemeVariant#INSTANCE}.
	 * @param themeVariant
	 */
	public final void setThemeVariant(@Nonnull IThemeVariant themeVariant) {
		UIContext.getRequestContext().setThemeVariant(themeVariant);
	}

	//public final IThemeVariant getThemeVariant() {
	//	return UIContext.getRequestContext().getThemeVariant();
	//}

	/**
	 * No longer in use for domui 2.0.
	 *
	 * Remove the styles that cause the margin kludge to be applied to all pages.
	 */
	@Deprecated
	public void unkludge() {
		removeCssClass("ui-content");
		removeCssClass("ui-kludge");
	}

	/**
	 * Gets called when a page is reloaded (for ROOT pages only).
	 */
	public void onReload() throws Exception {}

	/**
	 * Called when the page gets destroyed (navigation or such).
	 * @throws Exception
	 */
	public void onDestroy() throws Exception {}

	/**
	 * Get the page name used for {@link AppPageTitleBar} and {@link BreadCrumb} related code. To set the head title use the
	 * "title" property.
	 * @return
	 */
	@Nullable
	public String getPageTitle() {
		return m_pageTitle;
	}

	@Override
	@Nonnull
	public String getTestRepeatId() {
		return "";
	}

	/**
	 * Set the page name used for {@link AppPageTitleBar} and {@link BreadCrumb} related code. To set the head title use the
	 * "title" property.
	 *
	 * @param pageTitle
	 */
	public void setPageTitle(@Nullable String pageTitle) {
		m_pageTitle = pageTitle;
	}

	/**
	 * Adds javascript to close page window.
	 */
	public void closeWindow() {
		appendJavascript("window.close();");
	}

	/**
	 * In case that stretch layout is used, this needs to be called for UrlPage that does not have already set height on its body (usually case for normal UrlPage that is shown inside new browser popup window - it is not needed for regular subclasses of {@link Window}).
	 */
	protected void fixStretchBody() {
		//Since html and body are not by default 100% anymore we need to make it like this here in order to enable stretch to work.
		//We really need this layout support in domui!).
		appendCreateJS("$(document).ready(function() {$('body').addClass('ui-stretch-body');$('html').height('100%'); " + getCustomUpdatesCallJS() + "});");
	}

	/**
	 * In case that stretch layout is used, for page that does not use default ui-content padding (unkludged),
	 * this needs to be called for UrlPage that does not have already set height on its body.
	 * Usual case is that UrlPage is shown inside new browser popup window - so, it is not needed for regular subclasses of {@link Window}.
	 */
	protected void fixStretchBodyUnkludged() {
		//Since html and body are not by default 100% anymore we need to make it like this here in order to enable stretch to work.
		//We really need this layout support in domui!).
		appendCreateJS("$(document).ready(function() {$('html').height('100%');$('body').height('100%');" + getCustomUpdatesCallJS() + "});");
	}

	/**
	 * This is the root implementation to get the "shared context" for database access. Override this to get
	 * a different "default".
	 * @see to.etc.domui.dom.html.NodeBase#getSharedContext()
	 */
	@Override
	@Nonnull
	public QDataContext getSharedContext() throws Exception {
		return getSharedContext(QContextManager.DEFAULT);
	}

	@Nonnull
	public QDataContext getSharedContext(@Nonnull String key) throws Exception {
		return QContextManager.getContext(key, getPage().getContextContainer(key));
	}

	@Override
	@Nonnull
	public QDataContextFactory getSharedContextFactory() {
		return getSharedContextFactory(QContextManager.DEFAULT);
	}

	@Nonnull
	public QDataContextFactory getSharedContextFactory(@Nonnull String key) {
		return QContextManager.getDataContextFactory(key, getPage().getContextContainer(key));
	}

	@Override
	protected void onForceRebuild() {
		super.onForceRebuild();
		getPage().getConversation().setAttribute(LogicContextImpl.class.getName(), null);
	}

	/**
	 * EXPERIMENTAL Returns the business logic context for the current form.
	 * @see to.etc.domui.dom.html.NodeBase#lc()
	 */
	@Override
	@Nonnull
	public ILogicContext lc() throws Exception {
		ILogicContext lc = (ILogicContext) getPage().getConversation().getAttribute(LogicContextImpl.class.getName());
		if(null == lc) {
			lc = new LogicContextImpl(getSharedContext());
			getPage().getConversation().setAttribute(LogicContextImpl.class.getName(), lc);
		}
		return lc;
	}

	public void forceReloadData() throws Exception {
		resetAllSharedContexts();
		DomApplication.get().getInjector().injectPageValues(this, getPage().getPageParameters());    // Force reload of all parameters
		forceRebuild();
	}

	/**
	 * Use this only in cases when you really want to have fresh shared context -> in order to fetch all data fresh from database.
	 */
	public void resetAllSharedContexts() {
		getPage().getConversation().setAttribute(LogicContextImpl.class.getName(), null);            // Destroy any context
		QContextManager.closeSharedContexts(getPage().getConversation());            // Drop all connections
	}

	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if(Constants.ACDM_NOTIFY_PAGE.equals(action)) {
			handleNotifyPageCommand(ctx);
		} else {
			super.componentHandleWebAction(ctx, action);
		}
	}

	private void handleNotifyPageCommand(@Nonnull final IRequestContext ctx) throws Exception {
		String command = ctx.getParameter(getActualID() + "_command");
		INotifyPageEvent listener = getNotifyPageEvent();
		if(null != listener) {
			listener.execute(command);
		}
	}

	@Nullable
	public INotifyPageEvent getNotifyPageEvent() {
		return m_notifyPageEvent;
	}

	public void setNotifyPageEvent(@Nonnull INotifyPageEvent notifyPageEvent) {
		m_notifyPageEvent = notifyPageEvent;
	}
}

