package to.etc.domui.hibernate.generic;

import java.io.*;
import java.util.*;

import org.hibernate.*;

import to.etc.domui.hibernate.model.*;
import to.etc.webapp.query.*;

/**
 * This handler knows how to execute Hibernate queries using a basic Hibernate context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2010
 */
public class HibernateQueryHandler implements IAbstractQueryHandler<BuggyHibernateBaseContext>, IQueryHandlerFactory {
	static public final IQueryHandlerFactory FACTORY = new HibernateQueryHandler();

	protected HibernateQueryHandler() {}

	/*--------------------------------------------------------------*/
	/*	CODING:	IQAlternateContextFactory implementation.			*/
	/*--------------------------------------------------------------*/
	@Override
	public IAbstractQueryHandler< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
		return null; // Never acceptable
	}

	@Override
	public IAbstractQueryHandler< ? > findContextHandler(QDataContext root, QCriteriaQueryBase< ? > criteria) {
		return findContextHandler(root, criteria.getBaseClass());
	}

	@Override
	public IAbstractQueryHandler< ? > findContextHandler(QDataContext root, Class< ? > clz) {
		if(clz == null)
			return null;

		//-- Accept anything.
		return this;
	}

	@Override
	public IAbstractQueryHandler< ? > findContextHandler(QDataContext root, Object recordInstance) {
		return recordInstance == null ? null : this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAbstractQueryHandler implementation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * Delete the record passed.
	 */
	@Override
	public void delete(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().delete(o);
	}

	@Override
	public <T> T find(BuggyHibernateBaseContext root, Class<T> clz, Object pk) throws Exception {
		return (T) root.getSession().get(clz, (Serializable) pk);
	}

	@Override
	public <T> T getInstance(BuggyHibernateBaseContext root, Class<T> clz, Object pk) throws Exception {
		return (T) root.getSession().load(clz, (Serializable) pk); // Do not check if instance exists.
	}

	@Override
	public <T> List<T> query(BuggyHibernateBaseContext root, QCriteria<T> q) throws Exception {
		Criteria crit = GenericHibernateHandler.createCriteria(root.getSession(), q); // Convert to Hibernate criteria
		return crit.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> query(BuggyHibernateBaseContext root, QSelection< ? > sel) throws Exception {
		Criteria crit = GenericHibernateHandler.createCriteria(root.getSession(), sel);
		List resl = crit.list(); // Need to use raw class because ? is a monster fuckup
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
	public void refresh(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().refresh(o);
	}

	@Override
	public void save(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().save(o);
	}

	@Override
	public void attach(BuggyHibernateBaseContext root, Object o) throws Exception {
		root.getSession().update(o);
	}
}
