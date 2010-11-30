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
package to.etc.domui.ajax;

public class RpcException extends RuntimeException {
	/** The incoming request URL, if applicable. */
	private String m_url;

	/** The request type */
	private String m_method;

	/** The query string if this was a get */
	private String m_queryString;

	/** The remote address for this thingy. */
	private String m_remoteAddress;

	private final String m_message;

	public RpcException(final String message) {
		m_message = message;
	}

	public RpcException(final Throwable t, final String message) {
		super(t);
		m_message = message;
	}

	@Override
	public String getMessage() {
		return m_message;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(final String url) {
		m_url = url;
	}

	public String getMethod() {
		return m_method;
	}

	public void setMethod(final String method) {
		m_method = method;
	}

	public String getQueryString() {
		return m_queryString;
	}

	public void setQueryString(final String queryString) {
		m_queryString = queryString;
	}

	public String getRemoteAddress() {
		return m_remoteAddress;
	}

	public void setRemoteAddress(final String remoteAddress) {
		m_remoteAddress = remoteAddress;
	}
}
