package to.etc.domui.jpa.em;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.jpa.JpaConnector;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.IConversationStateListener;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.query.DefaultBeforeImageCache;
import to.etc.webapp.query.QAbstractDataContext;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaDataContext extends QAbstractDataContext implements QDataContext, IConversationStateListener {
    static protected final Logger LOG = LoggerFactory.getLogger(JpaDataContext.class);

    private final JpaEntityManagerFactory m_emFactory;

    private String m_conversationInvalid;

    private boolean m_ignoreClose;

    protected EntityManager m_session;

    @Nonnull
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
    public EntityManager getSession() throws Exception {
        checkValid();

        if(m_session == null) {
            EntityManager manager = m_session = m_emFactory.createEntityManager();
            manager.getTransaction().begin();
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

    @Override
    public void startTransaction() throws Exception {
        EntityManager em = getSession();
        if(!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    @Override
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

    @Override
    public boolean inTransaction() throws Exception {
        return getSession().getTransaction().isActive();
    }

    @Override
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

    @Override
    public Connection getConnection() throws Exception {
        startTransaction();
        return JpaConnector.getConnection(getSession());
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
    @Override
    public void conversationAttached(final ConversationContext cc) throws Exception {
        setConversationInvalid(null);
    }

    @Override
    public void conversationDestroyed(final ConversationContext cc) throws Exception {
        setIgnoreClose(false); // Disable ignore close - this close should work.
        close();
        setConversationInvalid("Conversation was destroyed");
    }

    @Override
    public void conversationDetached(final ConversationContext cc) throws Exception {
        setIgnoreClose(false); // Disable ignore close - this close should work.
        close();
        setConversationInvalid("Conversation is detached");
    }

    @Override
    public void conversationNew(final ConversationContext cc) throws Exception {}
}
