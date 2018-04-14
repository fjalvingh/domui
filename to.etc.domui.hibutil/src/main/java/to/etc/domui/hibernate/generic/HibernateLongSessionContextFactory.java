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
package to.etc.domui.hibernate.generic;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QEventListenerSet;
import to.etc.webapp.query.QQueryExecutorRegistry;

public class HibernateLongSessionContextFactory extends AbstractHibernateContextFactory {
	public HibernateLongSessionContextFactory(@NonNull QEventListenerSet eventSet, @NonNull HibernateSessionMaker sessionMaker, @NonNull QQueryExecutorRegistry handlers) {
		super(eventSet, sessionMaker, handlers);
	}

	public HibernateLongSessionContextFactory(@NonNull QEventListenerSet eventSet, @NonNull HibernateSessionMaker sessionMaker) {
		super(eventSet, sessionMaker);
	}

//	public HibernateLongSessionContextFactory(HibernateSessionMaker sessionMaker) {
//		super(sessionMaker);
//	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(this, getSessionMaker());
	}
}
