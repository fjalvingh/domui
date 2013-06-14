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
package to.etc.webapp.nls;

import java.util.*;

import javax.annotation.*;

/**
 * A list of message providers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2006
 */
public class NlsMessageProviderList implements NlsMessageProvider {
	/** The list of all message bundles that must be searched when rendering a global message. */
	private List<NlsMessageProvider> m_providerList = new ArrayList<NlsMessageProvider>();

	@Override
	public String findMessage(@Nonnull Locale loc, @Nonnull String code) {
		for(NlsMessageProvider p : m_providerList) {
			String msg = p.findMessage(loc, code);
			if(msg != null)
				return msg;
		}
		return null;
	}

	public void addProvider(NlsMessageProvider p) {
		m_providerList.add(p);
	}
}
