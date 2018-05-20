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
package to.etc.domui.trouble;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Thrown when access control is specified on a page but the user is not logged in.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2009
 */
final public class NotLoggedInException extends RuntimeException {
	private final String m_url;

	private NotLoggedInException(final String url) {
		super("You need to be logged in");
		m_url = url;
	}

	public static Exception create(String url) {
		return new NotLoggedInException(url);
	}

	public String getURL() {
		return m_url;
	}

	/**
	 * Create the proper exception type to return back to the specified page after login.
	 * @param ctx
	 * @return
	 */
	static public NotLoggedInException create(IRequestContext ctx, Page page) {
		//-- Create the after-login target URL.
		StringBuilder sb = new StringBuilder(256);
		sb.append(ctx.getRelativePath(ctx.getInputPath()));

		int len = sb.length();
		try {
			sb.append('?');
			StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
			sb.append('=');
			String sessionID = ctx.getWindowSession().getWindowID();
			sb.append(sessionID);
			// FIXME Not having a page here is VERY questionable!!!
			if(page != null) {
				sb.append('.').append(page.getConversation().getId());
				DomUtil.addUrlParameters(sb, page.getPageParameters(), false);
			} else {
				sb.append(".x");                                        // Dummy conversation ID
			}
		} catch(Exception x) {
			//-- Allow not having a window session
			sb.setLength(len);							// Remove crud added by failed code
			if(null != page)
				DomUtil.addUrlParameters(sb, page.getPageParameters(), true);
		}

		return new NotLoggedInException(sb.toString()); 			// Force login exception.
	}

	static public NotLoggedInException create() {
		IRequestContext ctx = UIContext.getRequestContext();
		Page page = UIContext.getCurrentPage();
		return create(ctx, page);
	}
}
