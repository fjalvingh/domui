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

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import org.hibernate.*;
import org.slf4j.*;

import to.etc.domui.state.*;
import to.etc.util.*;
import to.etc.webapp.core.*;
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

	@Nonnull
	private List<IRunnable> m_commitHandlerList = Collections.EMPTY_LIST;

	@Nullable
	private DefaultBeforeImageCache m_beforeCache;

	private boolean m_dataLoaded;

	private boolean m_keepOriginals;

	static private final boolean m_logCloses = System.getProperty("domui.trace.close") != null;

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
			m_session = m_sessionMaker.makeSession(this);
			m_session.beginTransaction();					// jal 20130321 Needed for Postgres to properly load lob's.
		}
		return m_session;
	}

	final protected void checkValid() {
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

		boolean logCloses = m_logCloses || DeveloperOptions.isDeveloperWorkstation();
		if(!logCloses) {
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
		if(!inTransaction())
			throw new IllegalStateException("Commit called without startTransaction."); // jal 20101028 Finally fix problem where commit fails silently.
		getSession().getTransaction().commit();
		runCommitHandlers();
		startTransaction();
	}

	protected void runCommitHandlers() throws Exception {
		Exception firstx = null;
		for(IRunnable r : m_commitHandlerList) {
			try {
				r.run();
			} catch(Exception x) {
				if(null == firstx)
					firstx = x;
				else
					x.printStackTrace();
			}
		}
		m_commitHandlerList.clear();
		if(null != firstx)
			throw firstx;
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

	@Override
	public <T> T original(T copy) {
		DefaultBeforeImageCache bc = m_beforeCache;
		if(null == bc)
			throw new IllegalStateException("Before caching is not enabled on this data context, call setKeepOriginals() before using it.");
		return bc.findBeforeImage(copy);
	}

	/**
	 *
	 * @see to.etc.webapp.query.QDataContext#setKeepOriginals()
	 */
	@Override
	public void setKeepOriginals() {
		if(m_keepOriginals)
			return;
		if(m_dataLoaded)
			throw new IllegalStateException("This data context has already been used to load data, you can only set the before images flag on an unused context");
		m_keepOriginals = true;
	}

	public boolean isKeepOriginals() {
		return m_keepOriginals;
	}

	@Nonnull
	public DefaultBeforeImageCache getBeforeCache() {
		DefaultBeforeImageCache beforeCache = m_beforeCache;
		if(null == beforeCache) {
			beforeCache = m_beforeCache = new DefaultBeforeImageCache();
		}
		return beforeCache;
	}

	/**
	 * We explicitly undeprecate here.
	 *
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#getConnection()
	 */
	@SuppressWarnings("deprecation")
	public Connection getConnection() throws Exception {
		startTransaction();
		return getSession().connection();
	}

	@Override
	public void addCommitAction(IRunnable cx) {
		if(m_commitHandlerList == Collections.EMPTY_LIST)
			m_commitHandlerList = new ArrayList<IRunnable>();
		m_commitHandlerList.add(cx);
	}

	@Override
	public <T> T find(Class<T> clz, Object pk) throws Exception {
		m_dataLoaded = true;
		return super.find(clz, pk);
	}

	@Override
	public <T> T getInstance(Class<T> clz, Object pk) throws Exception {
		m_dataLoaded = true;
		return super.getInstance(clz, pk);
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
