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
package to.etc.domui.util.db;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * EXPERIMENTAL INTERFACE
 * Pluggable interface to copy a source model to a target model.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 8, 2010
 */
public class QCopy {
	@Nullable
	static private IModelCopier m_copier;

	static public synchronized void setImplementation(@Nonnull IModelCopier m) {
		m_copier = m;
	}

	@Nonnull
	static synchronized private IModelCopier c() {
		if(null != m_copier)
			return m_copier;
		throw new IllegalStateException("QCopy.setImplementation() initialization method must be called before use.");
	}

	static synchronized public final IModelCopier getInstance() {
		return c();
	}

	static public <T> T copyInstanceShallow(QDataContext dc, T source) throws Exception {
		return c().copyInstanceShallow(dc, source);
	}

	static public <T> T copyDeep(QDataContext targetdc, QDataContext sourcedc, T source) throws Exception {
		return c().copyInstanceDeep(targetdc, sourcedc, source);
	}
}
