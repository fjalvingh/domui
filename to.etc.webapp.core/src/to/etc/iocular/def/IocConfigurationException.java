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
package to.etc.iocular.def;

import javax.annotation.*;

import to.etc.iocular.*;

public class IocConfigurationException extends IocException {
	private BasicContainerBuilder m_builder;

	private ComponentBuilder m_cb;

	private String m_location;

	public IocConfigurationException(@Nullable ComponentBuilder b, @Nonnull String message) {
		super(message);
		m_cb = b;
		if(b != null) {
			m_builder = b.getBuilder();
			m_location = b.getDefinitionLocation();
		}
	}


	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull String message) {
		super(message);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull Throwable cause) {
		super(cause);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
		m_builder = b;
		m_location = location;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getMessage());
		ComponentBuilder cb = m_cb;
		if(cb != null) {
			sb.append("\n- The object being built is: ");
			sb.append(cb.getIdent());
		}
		BasicContainerBuilder builder = m_builder;
		if(builder != null) {
			sb.append("\n- for the container with the name '");
			sb.append(builder.getName());
			sb.append("'");
		}
		if(m_location != null) {
			sb.append("\n- Defined at ");
			sb.append(m_location);
		}

		sb.append("\n\n");
		return sb.toString();
	}

	public String getLocationText() {
		return m_location;
	}
}
