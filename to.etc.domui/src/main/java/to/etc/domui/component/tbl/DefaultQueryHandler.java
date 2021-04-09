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

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.NodeBase;
import to.etc.webapp.query.QContextManager;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QSelection;

import java.util.List;

@NonNullByDefault
public class DefaultQueryHandler<T> implements IQueryHandler<T> {
	final private QDataContextFactory m_dcf;

	public DefaultQueryHandler(NodeBase b) {
		m_dcf = QContextManager.getDataContextFactory(QContextManager.DEFAULT, b.getPage().getConversation());
	}

	@Override
	public List<T> query(QCriteria<T> q) throws Exception {
		try(QDataContext dc = m_dcf.getDataContext()) {
			return dc.query(q);
		}
	}

	@Override public List<Object[]> query(QSelection<T> q) throws Exception {
		try(QDataContext dc = m_dcf.getDataContext()) {
			return dc.query(q);
		}
	}
}
