/*
 * DomUI Java User Interface - shared code
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
package to.etc.smtp;

import to.etc.util.*;

/**
 * An email recipient.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2006
 */
final public class Address {
	/** The @ email address. */
	private String	m_email;

	/** If applicable a name. */
	private String	m_name;

	public Address(String email) {
		if(email == null || !StringTool.isValidEmail(email))
			throw new IllegalStateException("The string '" + email + "' is not a valid email address.");
		m_email = email;
	}

	public Address(String email, String name) {
		this(email);
		if(name.trim().length() == 0)
			throw new IllegalStateException("Invalid name");
		m_name = name;
	}

	public String getEmail() {
		return m_email;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public String toString() {
		if(m_name != null)
			return m_name + " <" + m_email + ">";
		return m_email;
	}
}
