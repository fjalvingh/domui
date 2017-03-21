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
 * Something which can provide a message for a given code and locale.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 10, 2006
 */
public interface NlsMessageProvider {
	/**
	 * Locate the specified key for the specified locale. This does fallback, meaning that if the exact
	 * locale is not matched it will try a less restrictive one, until the empty (default) locale has
	 * been reached. The first match is returned; if not even the empty locale returns a match this
	 * returns null.
	 *
	 * @param loc
	 * @param code
	 * @return
	 */
	@Nullable
	String findMessage(@Nonnull Locale loc, @Nonnull String code);
}
