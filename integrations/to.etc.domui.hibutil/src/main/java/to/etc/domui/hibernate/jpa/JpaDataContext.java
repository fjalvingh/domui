package to.etc.domui.hibernate.jpa;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.IConversationStateListener;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.query.DefaultBeforeImageCache;
import to.etc.webapp.query.QAbstractDataContext;
import to.etc.webapp.query.QDataContext;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaDataContext extends QAbstractDataContext implements QDataContext, IConversationStateListener {
    static protected final Logger LOG = LoggerFactory.getLogger(JpaDataContext.class);

    private final JpaEntityManagerFactory m_emFactory;

    private String m_conversationInvalid;

    private boolean m_ignoreClose;

    protected EntityManager m_manager;

    @NonNull
    private List<IRunnable> m_commitHandlerList = Collections.EMPTY_LIST;

    @Nullable
    private DefaultBeforeImageCache m_beforeCache;

    private boolean m_dataLoaded;

    private boolean m_keepOriginals;

    static private final boolean m_logCloses = System.getProperty("domui.trace.close") != null;

    JpaDataContext(JpaEntityManagerFactory factory) {
        super(factory);
        m_emFactory = factory;
    }

    /**
     * INTERNAL USE ONLY Get the entity manager
     */
    public EntityManager getEntityManager() throws Exception {
        checkValid();

        if(m_manager == null) {
            EntityManager manager = m_manager = m_emFactory.createEntityManager();
            Session session = JpaConnector.getHibernateSession(manager);
            session.setFlushMode(FlushMode.COMMIT);     // EXPERIMENTAL Used to be "manual"
            if(!session.isConnected())
                LOG.debug("reconnecting session.");

            manager.getTransaction().begin();
        }
        return m_manager;
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
    @Override
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
     */
    @Override
    public void close() {
        if(m_manager == null || m_ignoreClose)
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

        try {
            if(m_manager.getTransaction().isActive()) {
                m_manager.getTransaction().rollback();
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
        try {
            m_manager.close();
        } catch(Exception x) {
            x.printStackTrace();
        }
        m_manager = null;
    }

    @Override
    public void startTransaction() throws Exception {
        EntityManager em = getEntityManager();
        if(!em.getTransaction().isActive())
            em.getTransaction().begin();
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

    @Override
    public boolean inTransaction() throws Exception {
        return getEntityManager().getTransaction().isActive();
    }

    @Override
    public void rollback() throws Exception {
        if(getEntityManager().getTransaction().isActive())
            getEntityManager().getTransaction().rollback();
    }

    @Override
    public <T> T original(T copy) {
        DefaultBeforeImageCache bc = m_beforeCache;
        if(null == bc)
            throw new IllegalStateException("Before caching is not enabled on this data context, call setKeepOriginals() before using it.");
        return bc.findBeforeImage(copy);
    }

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

    @NonNull
    public DefaultBeforeImageCache getBeforeCache() {
        DefaultBeforeImageCache beforeCache = m_beforeCache;
        if(null == beforeCache) {
            beforeCache = m_beforeCache = new DefaultBeforeImageCache();
        }
        return beforeCache;
    }

    @Override
    public Connection getConnection() throws Exception {
        startTransaction();
        return JpaConnector.getConnection(getEntityManager());
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
    /*	CODING:	Long-running requirements.                          */
    /*--------------------------------------------------------------*/
    @Override
    public void conversationDestroyed(final ConversationContext cc) throws Exception {
        if(m_manager == null)
            return;
        Session session = JpaConnector.getHibernateSession(m_manager);
        if(! session.isConnected())
            return;
        try {
            setConversationInvalid("Conversation was destroyed");
            setIgnoreClose(false);
            SessionImpl sim = (SessionImpl) session;
            StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();
            Map<?, ?> flups = spc.getEntitiesByKey();
            if(LOG.isDebugEnabled())
                LOG.debug("Hibernate: closing (destroying) session " + System.identityHashCode(m_manager) + " containing " + flups.size() + " persisted instances");
            if(m_manager.getTransaction().isActive())
                m_manager.getTransaction().rollback();
            close();
        } catch(Exception x) {
            LOG.info("Exception during conversation destroy: " + x, x);
        }
    }

    @Override
    public void conversationDetached(final ConversationContext cc) throws Exception {
        if(m_manager == null)
            return;
        Session session = JpaConnector.getHibernateSession(m_manager);
        if(! session.isConnected())
            return;
        setConversationInvalid("Conversation is detached");
        SessionImpl sim = (SessionImpl) session;
        StatefulPersistenceContext spc = (StatefulPersistenceContext) sim.getPersistenceContext();

        if(LOG.isDebugEnabled()) {
            Map< ? , ? > persisted = spc.getEntitiesByKey();
            LOG.debug("Hibernate: disconnecting entityManager "
                    + System.identityHashCode(m_manager) + " containing " + persisted.size()
                    + " persisted instances"
            );
        }
        if(m_manager.getTransaction().isActive())
            m_manager.getTransaction().rollback();
        session.disconnect();                               // disconnect underlying db connection
    }

    @Override
    public void conversationAttached(ConversationContext cc) {
        setConversationInvalid(null);
    }

    @Override
    public void conversationNew(ConversationContext cc) {
        setConversationInvalid(null);
    }

    /**
     * Commit; make sure a transaction exists (because nothing is flushed anyway) then commit.
     */
    @Override
    public void commit() throws Exception {
        startTransaction();
        m_manager.flush();
        getEntityManager().getTransaction().commit();
        runCommitHandlers();
        startTransaction();
    }

    /**
     * Should never be used on a long-used context (20091206 jal, error in table update if object not saved 1st).
     */
    @Override
    public void attach(Object o) {
    }
}
