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
package to.etc.domui.component.tbl;

import java.util.*;

/**
 * DO NOT USE IF YOUR DATA CAN CHANGE AND YOU ARE NOT UPDATING THOSE CHANGES HERE!!
 * This model uses a list to populate a table. It is meant to be used <i>only</i> if
 * the resulting model is maintained by yourself.
 *
 * Model for list-based data. The actual list instance will be maintained on updates.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 6, 2009
 */
public class SimpleListModel<T> extends TableListModelBase<T> {
	private List<T> m_list;

	public SimpleListModel(List<T> list) {
		m_list = list;
	}

	@Override
	public List<T> getList() throws Exception {
		return m_list;
	}
}
