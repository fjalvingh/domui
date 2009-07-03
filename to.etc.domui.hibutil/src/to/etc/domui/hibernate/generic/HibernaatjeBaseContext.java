package to.etc.domui.hibernate.generic;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.hibernate.*;

import to.etc.domui.hibernate.model.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

public class HibernaatjeBaseContext implements QDataContext, ConversationStateListener {
	protected HibernateSessionMaker		m_sessionMaker;
	protected Session					m_session;

	HibernaatjeBaseContext(final HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}
	protected void	setSessionMaker(final HibernateSessionMaker sm) {
		m_sessionMaker = sm;
	}

	public Session	getSession() throws Exception {
		if(m_session == null) {
			m_session = m_sessionMaker.makeSession();
		}
		return m_session;
	}

	public <T> T find(final Class<T> clz, final Object pk) throws Exception {
		return (T) getSession().load(clz, (Serializable)pk);
	}

	public <T> List<T> query(final QCriteria<T> q) throws Exception {
		Criteria	crit = GenericHibernateHandler.createCriteria(getSession(), q);	// Convert to Hibernate criteria
		return crit.list();
	}
	public <T> T queryOne(final QCriteria<T> q) throws Exception {
		List<T>		res = query(q);
		if(res.size() == 0)
			return null;
		if(res.size() > 1)
			throw new IllegalStateException("The criteria-query "+q+" returns "+res.size()+" results instead of one");
		return res.get(0);
	}

	public void	close() {
		if(m_session == null)
			return;
//		System.out.println("hib: closing && discarding Hibernate session");
		try {
			if(m_session.getTransaction().isActive()) {
				m_session.getTransaction().rollback();
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
		try {
			m_session.close();
		} catch(Exception x) {
			x.printStackTrace();
		}
		m_session = null;
	}

	public void attach(final Object o) throws Exception {
		getSession().update(o);
	}
	public void delete(final Object o) throws Exception {
		getSession().delete(o);
	}
	public void save(final Object o) throws Exception {
		getSession().save(o);
	}
	public void refresh(final Object o) throws Exception {
		getSession().refresh(o);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ConversationStateListener impl.						*/
	/*--------------------------------------------------------------*/

	public void conversationAttached(final ConversationContext cc) throws Exception {
	}

	public void conversationDestroyed(final ConversationContext cc) throws Exception {
		close();
	}

	public void conversationDetached(final ConversationContext cc) throws Exception {
		close();
	}

	public void conversationNew(final ConversationContext cc) throws Exception {
	}

	public void startTransaction() throws Exception {
		if(! inTransaction())
			getSession().beginTransaction();
	}
	public void commit() throws Exception {
		if(inTransaction())
			getSession().getTransaction().commit();
	}

	public boolean inTransaction() throws Exception {
		return getSession().getTransaction().isActive();
	}
	public void rollback() throws Exception {
		if(getSession().getTransaction().isActive())
			getSession().getTransaction().rollback();
	}
	public Connection getConnection() throws Exception {
		return getSession().connection();
	}
}

