package to.etc.domui.hibernate.jpa;

import org.hibernate.Criteria;
import org.hibernate.Session;
import to.etc.domui.hibernate.model.GenericHibernateHandler;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQueryExecutor;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QSelection;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaQueryExecutor implements IQueryExecutor<JpaDataContext>, IQueryExecutorFactory {
    public final static IQueryExecutorFactory FACTORY = new JpaQueryExecutor();

    /*--------------------------------------------------------------*/
    /*	CODING:	IQAlternateContextFactory implementation.			*/
    /*--------------------------------------------------------------*/
    @Override
    public IQueryExecutor< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
        return null; // Never acceptable
    }

    @Override
    public IQueryExecutor< ? > findContextHandler(QDataContext root, Class< ? > clz) {
        if(clz == null)
            return null;

        //-- Accept anything.
        return this;
    }

    @Override
    public IQueryExecutor< ? > findContextHandler(QDataContext root, Object recordInstance) {
        return recordInstance == null ? null : this;
    }

    /*--------------------------------------------------------------*/
    /*	CODING:	IAbstractQueryHandler implementation.				*/
    /*--------------------------------------------------------------*/
    @Override
    public void delete(JpaDataContext root, Object o) throws Exception {
        root.getEntityManager().remove(o);
    }

    @Override
    public <T> T find(JpaDataContext root, Class<T> clz, Object pk) throws Exception {
        return root.getEntityManager().find(clz, pk);
    }

    @Override
    public <T> T getInstance(JpaDataContext root, Class<T> clz, Object pk) throws Exception {
        return root.getEntityManager().getReference(clz, pk); // Do not check if instance exists.
    }

    @Override
    public <T> T find(JpaDataContext root, ICriteriaTableDef<T> metatable, Object pk) throws Exception {
        throw new IllegalStateException("Inapplicable call for " + getClass().getName());
    }

    @Override
    public <T> T getInstance(JpaDataContext root, ICriteriaTableDef<T> clz, Object pk) throws Exception {
        throw new IllegalStateException("Inapplicable call for " + getClass().getName());
    }

    @Override
    public <T> List<T> query(JpaDataContext root, QCriteria<T> q) throws Exception {
        Session hibernateSession = JpaConnector.getHibernateSession(root.getEntityManager());
        Criteria crit = GenericHibernateHandler.createCriteria(hibernateSession, q); // Convert to Hibernate criteria
        return crit.list();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Object[]> query(JpaDataContext root, QSelection< ? > sel) throws Exception {
        Session hibernateSession = JpaConnector.getHibernateSession(root.getEntityManager());
        Criteria crit = GenericHibernateHandler.createCriteria(hibernateSession, sel);
        List<Object[]> resl = crit.list();
        if(resl.size() == 0)
            return Collections.EMPTY_LIST;
        if(sel.getColumnList().size() == 1 && !(resl.get(0) instanceof Object[])) {
            //-- Re-wrap this result as a list of Object[].
            for(int i = resl.size(); --i >= 0;) {
                resl.set(i, new Object[]{resl.get(i)});
            }
        }
        return resl;
    }

    @Override
    public void refresh(JpaDataContext root, Object o) throws Exception {
        root.getEntityManager().refresh(o);
    }

    @Override
    public void save(JpaDataContext root, Object o) throws Exception {
        root.getEntityManager().persist(o);
    }

    @Override
    public void attach(JpaDataContext root, Object o) throws Exception {
        root.getEntityManager().refresh(o);
    }
}
