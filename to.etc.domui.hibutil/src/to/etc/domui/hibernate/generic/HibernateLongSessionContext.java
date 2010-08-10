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
 * all intermediary flushes will be rolled back (and of course Hibernate will not see it because it
 * is utterly stupid).
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
		setConversationInvalid("Conversation was destroyed");
		setIgnoreClose(false);
		SessionImpl sim = (SessionImpl) m_session;
		StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();
		Map< ? , ? > flups = spc.getEntitiesByKey();
		if(LOG.isDebugEnabled())
			LOG.debug("Hibernate: closing (destroying) session " + System.identityHashCode(m_session) + " containing " + flups.size() + " persisted instances");
		if(m_session.getTransaction().isActive())
			m_session.getTransaction().rollback();
		close();
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

	@Override
	public void commit() throws Exception {
		m_session.flush();
		super.commit();
	}

	/**
	 * Should never be used on a long-used context (20091206 jal, error in table update if object not saved 1st).
	 * @see to.etc.domui.hibernate.generic.BuggyHibernateBaseContext#attach(java.lang.Object)
	 */
	@Override
	public void attach(Object o) throws Exception {
	}
}
