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

import to.etc.webapp.query.*;

abstract public class AbstractHibernateContextFactory implements QDataContextFactory {
	private HibernateSessionMaker m_sessionMaker;

	private QEventListenerSet m_eventSet;

	private QQueryExecutorRegistry m_handlers;

	static private QQueryExecutorRegistry m_default = new QQueryExecutorRegistry();

	static {
		m_default.register(HibernateQueryExecutor.FACTORY);
	}

	public AbstractHibernateContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryExecutorRegistry handlers) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
		m_handlers = handlers;
	}

	public AbstractHibernateContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
		m_handlers = m_default;
	}

	public AbstractHibernateContextFactory(HibernateSessionMaker sessionMaker) {
		m_eventSet = new QEventListenerSet();
		m_sessionMaker = sessionMaker;
		m_handlers = m_default;
	}

	protected HibernateSessionMaker getSessionMaker() {
		return m_sessionMaker;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	abstract public QDataContext getDataContext() throws Exception;

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getEventListeners()
	 */
	public QEventListenerSet getEventListeners() {
		return m_eventSet;
	}

	@Override
	public QQueryExecutorRegistry getQueryHandlerList() {
		return m_handlers;
	}
}
