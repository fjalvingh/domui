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

import java.util.*;

import org.hibernate.*;
import org.hibernate.engine.*;
import org.hibernate.impl.*;

import to.etc.domui.state.*;
import to.etc.webapp.query.*;

/**
 * A context that keeps the session alive but in disconnected mode while running. The session
 * is put into MANUAL flush mode, and the database connection is closed every time the conversation
 * is detached. Only the last phase of a conversation may commit and flush changes to the database;
 * all intermediary flushes will be rolled back (and of course Hibernate will not see it).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 23, 2008
 */
public class HibernateLongSessionContext extends BuggyHibernateBaseContext {
	public HibernateLongSessionContext(final QDataContextFactory src, final HibernateSessionMaker sessionMaker) {
		super(sessionMaker, src);
	}

	/**
	 * This override allocates a session in flushmode manual.
	 * @see to.etc.domui.hibernate.generic.BuggyHibernateBaseContext#getSession()
	 */
	@Override
	public Session getSession() throws Exception {
		checkValid();
		if(m_session == null) {
			super.getSession();
			m_session.setFlushMode(FlushMode.MANUAL);
		}
		if(!m_session.isConnected())
			LOG.debug("Hibernate: reconnecting session.");
		return m_session;
	}

	@Override
	public void conversationDestroyed(final ConversationContext cc) throws Exception {
		if(m_session == null || !m_session.isConnected())
			return;
		try {
			setConversationInvalid("Conversation was destroyed");
			setIgnoreClose(false);
			SessionImpl sim = (SessionImpl) m_session;
			StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();
			Map<?, ?> flups = spc.getEntitiesByKey();
			if(LOG.isDebugEnabled())
				LOG.debug("Hibernate: closing (destroying) session " + System.identityHashCode(m_session) + " containing " + flups.size() + " persisted instances");
			if(m_session.getTransaction().isActive())
				m_session.getTransaction().rollback();
			close();
		} catch(Exception x) {
			LOG.info("Exception during conversation destroy: " + x, x);
		}
	}

	@Override
	public void conversationDetached(final ConversationContext cc) throws Exception {
		if(m_session == null || !m_session.isConnected())
			return;
		setConversationInvalid("Conversation is detached");
		SessionImpl sim = (SessionImpl) m_session;
		StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();
		Map< ? , ? > flups = spc.getEntitiesByKey();
		if(LOG.isDebugEnabled())
			LOG.debug("Hibernate: disconnecting session " + System.identityHashCode(m_session) + " containing " + flups.size() + " persisted instances");
		if(m_session.getTransaction().isActive())
			m_session.getTransaction().rollback();
		m_session.disconnect(); // Disconnect the dude.
		//		if(m_session.isConnected())
		//			System.out.println("Session connected after disconnect ;-)");
	}

	@Override
	public void conversationAttached(ConversationContext cc) throws Exception {
		setConversationInvalid(null);
	}

	@Override
	public void conversationNew(ConversationContext cc) throws Exception {
		setConversationInvalid(null);
	}

	/**
	 * Commit; make sure a transaction exists (because nothing is flushed anyway) then commit.
	 * @see to.etc.domui.hibernate.generic.BuggyHibernateBaseContext#commit()
	 */
	@Override
	public void commit() throws Exception {
		startTransaction();
		m_session.flush();
		super.commit();
		startTransaction();
	}

	/**
	 * Should never be used on a long-used context (20091206 jal, error in table update if object not saved 1st).
	 * @see to.etc.domui.hibernate.generic.BuggyHibernateBaseContext#attach(java.lang.Object)
	 */
	@Override
	public void attach(Object o) throws Exception {
	}
}
