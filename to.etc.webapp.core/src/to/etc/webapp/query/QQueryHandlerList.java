package to.etc.webapp.query;

import java.util.*;

/**
 * This represents a list of registered query handlers that together should be
 * able to execute all of the queries asked on them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2010
 */
final public class QQueryHandlerList {
	private List<IQueryExecutorFactory> m_queryRendererList = Collections.EMPTY_LIST;

	static private final QQueryHandlerList m_instance = new QQueryHandlerList();

	static public final QQueryHandlerList getInstance() {
		return m_instance;
	}

	public synchronized void register(IQueryExecutorFactory cf) {
		m_queryRendererList = new ArrayList<IQueryExecutorFactory>(m_queryRendererList);
		m_queryRendererList.add(cf);
	}

	public synchronized List<IQueryExecutorFactory> getQueryRendererList() {
		return m_queryRendererList;
	}

	public IQueryExecutor<QDataContext> getHandler(QDataContext root, Class< ? > instanceClass) {
		List<IQueryExecutorFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryExecutorFactory xf = res.get(i);
			IQueryExecutor<QDataContext> xc = (IQueryExecutor<QDataContext>) xf.findContextHandler(root, instanceClass);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a query on class=" + instanceClass);
	}

	public IQueryExecutor<QDataContext> getHandler(QDataContext root, Object recordInstance) {
		List<IQueryExecutorFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryExecutorFactory xf = res.get(i);
			IQueryExecutor<QDataContext> xc = (IQueryExecutor<QDataContext>) xf.findContextHandler(root, recordInstance);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a data action on record Instance=" + recordInstance);
	}


	public IQueryExecutor<QDataContext> getHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
		List<IQueryExecutorFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryExecutorFactory xf = res.get(i);
			IQueryExecutor<QDataContext> xc = (IQueryExecutor<QDataContext>) xf.findContextHandler(root, tableMeta);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a query on meta-table=" + tableMeta);
	}

	public IQueryExecutor<QDataContext> getHandler(QDataContext root, QCriteriaQueryBase< ? > query) {
		if(query.getBaseClass() != null)
			return getHandler(root, query.getBaseClass());
		else
			return getHandler(root, query.getMetaTable());
	}
}
