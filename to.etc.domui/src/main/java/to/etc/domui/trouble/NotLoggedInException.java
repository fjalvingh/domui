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
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Thrown when access control is specified on a page but the user is not logged in.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2009
 */
public class NotLoggedInException extends RuntimeException {
	private final String m_url;

	public NotLoggedInException(final String url) {
		super("You need to be logged in");
		m_url = url;
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
		//				sb.append('/');
		sb.append(ctx.getRelativePath(ctx.getInputPath()));
		sb.append('?');
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(ctx.getWindowSession().getWindowID());

		// FIXME Not having a page here is VERY questionable!!!
		if(page != null)
			sb.append('.').append(page.getConversation().getId());
		else
			sb.append(".x"); // Dummy conversation ID
		DomUtil.addUrlParameters(sb, ctx, false);
		return new NotLoggedInException(sb.toString()); // Force login exception.
	}
}
