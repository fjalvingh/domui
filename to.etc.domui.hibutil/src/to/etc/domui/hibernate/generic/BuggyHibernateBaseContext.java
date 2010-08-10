package to.etc.domui.hibernate.generic;

import java.sql.*;

import org.hibernate.*;
import org.slf4j.*;

import to.etc.domui.state.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This is a basic Hibernate QDataContext implementation, suitable for
 * being used in DomUI code. This base class implements every QDataContext call
 * but does not do any session lifecycle handling.
 *
 * FIXME 20100310 jal This now supports JDBC queries using the same JDBC context but with
 * a butt-ugly mechanism; it needs to be replaced with some kind of switch proxy implementation
 * later on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class BuggyHibernateBaseContext extends QAbstractDataContext implements QDataContext, IConversationStateListener {
	static protected final Logger LOG = LoggerFactory.getLogger(BuggyHibernateBaseContext.class);

	private String m_conversationInvalid;

	protected HibernateSessionMaker m_sessionMaker;

	private boolean m_ignoreClose;

	protected Session m_session;

	/**
	 * Create a context, using the specified factory to create Hibernate sessions.
	 * @param sessionMaker
	 */
	BuggyHibernateBaseContext(final HibernateSessionMaker sessionMaker, QDataContextFactory src) {
		super(src);
		m_sessionMaker = sessionMaker;
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
		checkValid();
		if(m_session == null) {
			m_session = m_sessionMaker.makeSession();
		}
		return m_session;
	}

	protected void checkValid() {
		if(m_conversationInvalid != null)
			throw new IllegalStateException("You cannot use this QDataContext: " + m_conversationInvalid);
	}

	protected void setConversationInvalid(String conversationInvalid) {
		m_conversationInvalid = conversationInvalid;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	QDataContext implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#setIgnoreClose(boolean)
	 */
	public void setIgnoreClose(boolean on) {
		m_ignoreClose = on;
	}

	public boolean isIgnoreClose() {
		return m_ignoreClose;
	}

	static private final String[] PRESET = {"to.etc.dbpool.", "oracle.", "nl.itris.viewpoint.db.hibernate."};

	static private final String[] ENDSET = {"to.etc.dbpool.", "to.etc.domui.server.", "org.apache.tomcat"};

	/**
	 * This version just delegates to the Factory immediately.
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#close()
	 */
	public void close() {
		if(m_session == null || m_ignoreClose)
			return;

		if(!DeveloperOptions.isDeveloperWorkstation()) {
			setConversationInvalid("DataContext has been CLOSED");
		} else {
			//-- Log close location when running on development
			StringBuilder sb = new StringBuilder();
			sb.append("DataContext has been CLOSED");

			Exception mxx = null;
			try {
				throw new Exception();
			} catch(Exception x) {
				mxx = x;
			}
			if(mxx != null) {
				sb.append("\nClose location:\n");
				StringTool.strStacktraceFiltered(sb, mxx, PRESET, ENDSET, 40);
			}
			setConversationInvalid(sb.toString());
		}

		//		System.out.println("..... closing hibernate session: "+System.identityHashCode(m_session));
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
	 * We explicitly undeprecate here.
	 *
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#getConnection()
	 */
	@SuppressWarnings("deprecation")
	public Connection getConnection() throws Exception {
		return getSession().connection();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ConversationStateListener impl.						*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	public void conversationAttached(final ConversationContext cc) throws Exception {
		setConversationInvalid(null);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.IConversationStateListener#conversationDestroyed(to.etc.domui.state.ConversationContext)
	 */
	public void conversationDestroyed(final ConversationContext cc) throws Exception {
		setIgnoreClose(false); // Disable ignore close - this close should work.
		close();
		setConversationInvalid("Conversation was destroyed");
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.IConversationStateListener#conversationDetached(to.etc.domui.state.ConversationContext)
	 */
	public void conversationDetached(final ConversationContext cc) throws Exception {
		setIgnoreClose(false); // Disable ignore close - this close should work.
		close();
		setConversationInvalid("Conversation is detached");
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.state.IConversationStateListener#conversationNew(to.etc.domui.state.ConversationContext)
	 */
	public void conversationNew(final ConversationContext cc) throws Exception {}
}
