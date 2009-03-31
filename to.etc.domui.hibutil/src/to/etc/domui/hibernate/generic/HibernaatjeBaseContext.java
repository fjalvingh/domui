package to.etc.domui.hibernate.generic;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.hibernate.*;

import to.etc.domui.hibernate.model.*;
import to.etc.domui.state.*;
import to.etc.domui.util.query.*;

public class HibernaatjeBaseContext implements QDataContext, ConversationStateListener {
	protected HibernateSessionMaker		m_sessionMaker;
	protected Session					m_session;

	HibernaatjeBaseContext(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	public Session	getSession() throws Exception {
		if(m_session == null) {
			m_session = m_sessionMaker.makeSession();
		}
		return m_session;
	}

	public <T> T find(Class<T> clz, Object pk) throws Exception {
		return (T) getSession().load(clz, (Serializable)pk);
	}

	public <T> List<T> query(QCriteria<T> q) throws Exception {
		Criteria	crit = GenericHibernateHandler.createCriteria(getSession(), q);	// Convert to Hibernate criteria
		return crit.list();
	}
	public <T> T queryOne(QCriteria<T> q) throws Exception {
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

	public void attach(Object o) throws Exception {
		getSession().update(o);
	}
	public void delete(Object o) throws Exception {
		getSession().delete(o);
	}
	public void save(Object o) throws Exception {
		getSession().save(o);
	}
	public void refresh(Object o) throws Exception {
		getSession().refresh(o);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ConversationStateListener impl.						*/
	/*--------------------------------------------------------------*/
	
	public void conversationAttached(ConversationContext cc) throws Exception {
	}

	public void conversationDestroyed(ConversationContext cc) throws Exception {
		close();
	}

	public void conversationDetached(ConversationContext cc) throws Exception {
		close();
	}

	public void conversationNew(ConversationContext cc) throws Exception {
	}

	public void startTransaction() throws Exception {
		getSession().beginTransaction();
	}
	public void commit() throws Exception {
		getSession().getTransaction().commit();
	}
	public void rollback() throws Exception {
		if(getSession().getTransaction().isActive())
			getSession().getTransaction().rollback();
	}
	public Connection getConnection() throws Exception {
		return getSession().connection();
	}
}

