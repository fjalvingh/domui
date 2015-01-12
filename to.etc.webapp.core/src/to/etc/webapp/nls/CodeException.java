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

import java.text.*;
import java.util.*;

import javax.annotation.*;

/**
 * Base class for all code-based exceptions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 31, 2009
 */
public class CodeException extends RuntimeException {
	@Nonnull
	private final BundleRef m_bundle;

	@Nonnull
	private final String m_code;

	@Nonnull
	private final Object[] m_parameters;

	public CodeException(@Nonnull final BundleRef bundle, @Nonnull final String code, final Object... parameters) {
		if(bundle == null || code == null)
			throw new IllegalArgumentException("Bundle or code cannot be null");
		m_bundle = bundle;
		m_code = code;
		m_parameters = parameters;
	}

	public CodeException(@Nonnull final Throwable t, @Nonnull final BundleRef bundle, @Nonnull final String code, final Object... parameters) {
		super(t);
		if(bundle == null || code == null)
			throw new IllegalArgumentException("Bundle or code cannot be null");
		m_bundle = bundle;
		m_code = code;
		m_parameters = parameters;
	}

	@Nonnull
	public BundleRef getBundle() {
		return m_bundle;
	}

	@Nonnull
	public String getCode() {
		return m_code;
	}

	@Nullable
	public Object[] getParameters() {
		return m_parameters;
	}

	@Override
	public String getMessage() {
		Locale loc = NlsContext.getLocale();
		String msg = m_bundle.getString(m_code);
		MessageFormat temp = new MessageFormat(msg, loc); // SUN people are dumb. It's idiotic to have to create an object for this.
		return temp.format(m_parameters);
	}
}
