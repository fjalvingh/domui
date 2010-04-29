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
	private List<IQueryHandlerFactory> m_queryRendererList = Collections.EMPTY_LIST;

	static private final QQueryHandlerList m_instance = new QQueryHandlerList();

	static public final QQueryHandlerList getInstance() {
		return m_instance;
	}

	public synchronized void register(IQueryHandlerFactory cf) {
		m_queryRendererList = new ArrayList<IQueryHandlerFactory>(m_queryRendererList);
		m_queryRendererList.add(cf);
	}

	public synchronized List<IQueryHandlerFactory> getQueryRendererList() {
		return m_queryRendererList;
	}

	public IAbstractQueryHandler<QDataContext> getHandler(QDataContext root, Class< ? > instanceClass) {
		List<IQueryHandlerFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryHandlerFactory xf = res.get(i);
			IAbstractQueryHandler<QDataContext> xc = (IAbstractQueryHandler<QDataContext>) xf.findContextHandler(root, instanceClass);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a query on class=" + instanceClass);
	}

	public IAbstractQueryHandler<QDataContext> getHandler(QDataContext root, Object recordInstance) {
		List<IQueryHandlerFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryHandlerFactory xf = res.get(i);
			IAbstractQueryHandler<QDataContext> xc = (IAbstractQueryHandler<QDataContext>) xf.findContextHandler(root, recordInstance);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a data action on record Instance=" + recordInstance);
	}


	public IAbstractQueryHandler<QDataContext> getHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
		List<IQueryHandlerFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryHandlerFactory xf = res.get(i);
			IAbstractQueryHandler<QDataContext> xc = (IAbstractQueryHandler<QDataContext>) xf.findContextHandler(root, tableMeta);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a query on meta-table=" + tableMeta);
	}

	public IAbstractQueryHandler<QDataContext> getHandler(QDataContext root, QCriteriaQueryBase< ? > query) {
		List<IQueryHandlerFactory> res = getQueryRendererList();
		for(int i = 0; i < res.size(); i++) {
			IQueryHandlerFactory xf = res.get(i);
			IAbstractQueryHandler<QDataContext> xc = (IAbstractQueryHandler<QDataContext>) xf.findContextHandler(root, query);
			if(xc != null)
				return xc;
		}
		throw new IllegalStateException("None of the QQueryHandlerList's registered accepts a query on QCriteria=" + query);
	}
}
