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
import to.etc.domui.annotations.UIRights;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.login.IUser;
import to.etc.domui.login.UILogin;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IServerSession;
import to.etc.domui.trouble.NotLoggedInException;
import to.etc.domui.util.IRightsCheckedManually;

import java.io.File;

/**
 * A class which allows access to the page's context and related information. This
 * is experimental. The PageContext is the root for all navigational information,
 * and interfaces the pages and the server. This would usually be the task of the
 * RequestContext, but that's an interface and I want the primary accessor to be
 * in the same class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class UIContext {
	static public long FAILLOGIN_TIMEOUT = 5*60*1000;

	/** After this amount of failed logins just pretend we're logging in */
	static public int MAXFAILLOGINS = 10;

	static private ThreadLocal<IRequestContext> m_current = new ThreadLocal<IRequestContext>();

	static private ThreadLocal<Page> m_page = new ThreadLocal<Page>();

	@NonNull
	static public IRequestContext getRequestContext() {
		IRequestContext rc = m_current.get();
		if(rc == null)
			throw new IllegalStateException("No current request!");
		return rc;
	}

	/**
	 * Called when a new request is to be made current, or when the request has
	 * ended.
	 * @param rc
	 */
	static public void internalSet(@NonNull final IRequestContext rc) throws Exception {
		m_current.set(rc);
		boolean ok = false;
		try {
			UILogin.internalSetLoggedInUser(rc);
			ok = true;
		} finally {
			if(!ok)
				internalClear();
		}
	}

	/**
	 * This CLEARS all "current state" threadlocals.
	 */
	static public void internalClear() {
		m_current.set(null);
		UILogin.setCurrentUser(null);
		m_page.set(null);
	}

	static public void internalSet(final Page pg) {
		m_page.set(pg);
	}

	@NonNull
	static public Page getCurrentPage() {
		Page pg = m_page.get();
		if(pg == null)
			throw new IllegalStateException("No current page");
		return pg;
	}

	@Nullable
	static public Page internalGetPage() {
		return m_page.get();
	}

	@Nullable
	static public IRequestContext internalGetContext() {
		return m_current.get();
	}

	@NonNull
	static public ConversationContext getCurrentConversation() {
		return getCurrentPage().getConversation();
	}

	/**
	 * Return the currently-known logged in user, or null if unknown/not logged in.
	 * FIXME Should be named findCurrentUser().
	 * @return
	 */
	@Nullable
	static public IUser getCurrentUser() {
		return UILogin.getCurrentUser();
	}

	static public void setCurrentUser(IUser user) {
		UILogin.setCurrentUser(user);
	}

	/**
	 * This returns the currently logged in user. If the user is not logged in this throws
	 * a login exception which should cause the user to log in.
	 * @return
	 */
	@NonNull
	static public IUser getLoggedInUser() {
		IUser u = getCurrentUser();
		if(u == null)
			throw NotLoggedInException.create(getRequestContext(), getCurrentPage());
		return u;
	}

	/**
	 * Register a file as a file/directory to be deleted when the conversation terminates.
	 * @param tmpf
	 */
	static public void registerTempFile(@NonNull File tmpf) {
		ConversationContext cc = getCurrentConversation();
		cc.registerTempFile(tmpf);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Session attribute accessors.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns session value of expected type. Must be used within existing UIContext. In case that attribute is not stored in session, it would store defaultValue and return it.
	 * See {@link UIContext#getSessionAttribute(Class, String)
	 * See {@link UIContext#setSessionAttribute(String, Object)
	 *
	 * @param clz
	 * @param attrName
	 * @param defaultValue
	 * @return
	 */
	public static <T> T getSessionAttribute(Class<T> clz, String attrName, T defaultValue) {
		T val = getSessionAttribute(clz, attrName);
		if(val != null) {
			return val;
		}
		setSessionAttribute(attrName, defaultValue);
		return defaultValue;
	}

	/**
	 * Returns session value of expected type. Must be used within existing UIContext.
	 * @param clz
	 * @param attrName
	 * @return  In case that value is not stored returns null. In case of expected type mismatch throws IllegalStateException.
	 */
	public static <T> T getSessionAttribute(Class<T> clz, String attrName) {
		IRequestContext ctx = getRequestContext();
		IServerSession hs = ctx.getServerSession(false);
		if(null == hs)
			return null;

		Object val = hs.getAttribute(attrName);
		if(val != null) {
			if(clz.isAssignableFrom(val.getClass())) {
				T res = (T) val;
				return res;
			} else {
				throw new IllegalStateException("Session value of unexpected type: " + val.getClass().getCanonicalName() + ", expecting " + clz.getCanonicalName());
			}
		}
		return null;
	}

	/**
	 * Sets session attribute value.
	 */
	public static void setSessionAttribute(String attrName, Object value) {
		IRequestContext ctx = getRequestContext();
		IServerSession hs = ctx.getServerSession(true);
		if(null == hs)
			return;
		hs.setAttribute(attrName, value);
	}

	/**
	 * Checks whether the currently logged in (or not logged in) user has rights on
	 * the specified page, based on the class only. This does not implement either
	 * page-specific rights checking nor datapath based checking.
	 */
	public static boolean hasRightsOn(Class<? extends UrlPage> pageClass) {
		UIRights rann = pageClass.getAnnotation(UIRights.class);// Get class annotation
		if(rann == null) {										// Any kind of rights checking is required?
			return true;										// No -> allow access.
		}
		if(IRightsCheckedManually.class.isAssignableFrom(pageClass))
			throw new IllegalArgumentException("This is unsupported for " + pageClass + " as this implements IRightsCheckedManually");


		//-- Get user's IUser; if not present we need to log in.
		IUser user = UIContext.getCurrentUser(); 				// Currently logged in?
		if(user == null) {
			return false;										// No -> no access
		}

		//-- Start access checks, in order. First call the interface, if applicable
		String failureReason = null;
		try {
			return checkRightsAnnotation(pageClass, rann, user);
		} catch(Exception x) {
			x.printStackTrace();
			return false;
		}
	}

	static private boolean checkRightsAnnotation(@NonNull Class<? extends UrlPage> pageClass, @NonNull UIRights rann, @NonNull IUser user) throws Exception {
		//-- No special data context - we just check plain general rights
		if(rann.value().length == 0)						// No rights specified means -> just log in
			return true;

		for(String right : rann.value()) {
			if(user.hasRight(right)) {
				return true;
			}
		}
		return false;										// All worked, so we have access.
	}
}
