package to.etc.domui.hibernate.generic;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.hibernate.*;

import to.etc.domui.hibernate.model.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

/**
 * This is a basic Hibernate QDataContext implementation, suitable for
 * being used in DomUI code. This base class implements every QDataContext call
 * but does not do any session lifecycle handling.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class HibernaatjeBaseContext implements QDataContext, ConversationStateListener {
	private QDataContextSource		m_contextSource;
	protected HibernateSessionMaker m_sessionMaker;

	protected Session m_session;

	/**
	 * Create a context, using the specified factory to create Hibernate sessions.
	 * @param sessionMaker
	 */
	HibernaatjeBaseContext(final HibernateSessionMaker sessionMaker, QDataContextSource src) {
		m_sessionMaker = sessionMaker;
		m_contextSource = src;
	}

	/**
	 * Set the Hibernate session maker factory.
	 * @param sm
	 */
	protected void setSessionMaker(final HibernateSessionMaker sm) {
		m_sessionMaker = sm;
	}

	/**
	 * INTERNAL USE ONLY Get the Hibernate session present in this QDataContext; allocate a new
	 * Session if no session is currently active. This is not supposed to be called by user code.
	 * @return
	 * @throws Exception
	 */
	public Session getSession() throws Exception {
		if(m_session == null) {
			m_session = m_sessionMaker.makeSession();
		}
		return m_session;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	QDataContext implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	public QDataContextSource getSource() {
		return m_contextSource;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#find(java.lang.Class, java.lang.Object)
	 */
	public <T> T find(final Class<T> clz, final Object pk) throws Exception {
		return (T) getSession().get(clz, (Serializable) pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#getInstance(java.lang.Class, java.lang.Object)
	 */
	public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		return (T) getSession().load(clz, (Serializable) pk);		// Do not check if instance exists.
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#query(to.etc.webapp.query.QCriteria)
	 */
	public <T> List<T> query(final QCriteria<T> q) throws Exception {
		Criteria crit = GenericHibernateHandler.createCriteria(getSession(), q); // Convert to Hibernate criteria
		return crit.list();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#query(to.etc.webapp.query.QSelection)
	 */
	public List<Object[]> query(QSelection< ? > sel) throws Exception {
		Criteria crit = GenericHibernateHandler.createCriteria(getSession(), sel);
		return crit.list();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#queryOne(to.etc.webapp.query.QCriteria)
	 */
	public <T> T queryOne(final QCriteria<T> q) throws Exception {
		List<T> res = query(q);
		if(res.size() == 0)
			return null;
		if(res.size() > 1)
			throw new IllegalStateException("The criteria-query " + q + " returns " + res.size() + " results instead of one");
		return res.get(0);
	}

	/**
	 * Discard the context. All changed data gets discarded, and the transaction, if active, will be rolled back.
	 */
	public void close() {
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

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#attach(java.lang.Object)
	 */
	public void attach(final Object o) throws Exception {
		getSession().update(o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#delete(java.lang.Object)
	 */
	public void delete(final Object o) throws Exception {
		getSession().delete(o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#save(java.lang.Object)
	 */
	public void save(final Object o) throws Exception {
		getSession().save(o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#refresh(java.lang.Object)
	 */
	public void refresh(final Object o) throws Exception {
		getSession().refresh(o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#startTransaction()
	 */
	public void startTransaction() throws Exception {
		if(!inTransaction())
			getSession().beginTransaction();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#commit()
	 */
	public void commit() throws Exception {
		if(inTransaction())
			getSession().getTransaction().commit();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#inTransaction()
	 */
	public boolean inTransaction() throws Exception {
		return getSession().getTransaction().isActive();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#rollback()
	 */
	public void rollback() throws Exception {
		if(getSession().getTransaction().isActive())
			getSession().getTransaction().rollback();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#getConnection()
	 */
	public Connection getConnection() throws Exception {
		return getSession().connection();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ConversationStateListener impl.						*/
	/*--------------------------------------------------------------*/

	/**
	 * {@inheritDoc}
	 */
	public void conversationAttached(final ConversationContext cc) throws Exception {}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.ConversationStateListener#conversationDestroyed(to.etc.domui.state.ConversationContext)
	 */
	public void conversationDestroyed(final ConversationContext cc) throws Exception {
		close();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.ConversationStateListener#conversationDetached(to.etc.domui.state.ConversationContext)
	 */
	public void conversationDetached(final ConversationContext cc) throws Exception {
		close();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.ConversationStateListener#conversationNew(to.etc.domui.state.ConversationContext)
	 */
	public void conversationNew(final ConversationContext cc) throws Exception {}

}
