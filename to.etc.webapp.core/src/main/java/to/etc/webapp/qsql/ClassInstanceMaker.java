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
package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

class ClassInstanceMaker extends JdbcCompoundType implements IInstanceMaker {
	private int m_startIndex;

	public ClassInstanceMaker(PClassRef root, int startIndex, JdbcClassMeta cm) {
		super(cm);
		m_startIndex = startIndex;
	}

	/**
	 * Traverse all properties and obtain their value from the result set.
	 * @see to.etc.webapp.qsql.IInstanceMaker#make(java.sql.ResultSet)
	 */
	@Override
	public Object make(@Nonnull QDataContext dc, @Nonnull ResultSet rs) throws Exception {
		Object inst = convertToInstance(rs, m_startIndex, new JdbcPropertyMeta());
		if(inst instanceof IInitializable) {
			((IInitializable) inst).initializeInstance(dc);
		}
		return inst;
	}
}
