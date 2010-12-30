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
package to.etc.webapp.query;

import java.util.*;

/**
 * This represents a list of registered query handlers that together should be
 * able to execute all of the queries asked on them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2010
 */
final public class QQueryExecutorRegistry {
	private List<IQueryExecutorFactory> m_queryRendererList = Collections.EMPTY_LIST;

	static private final QQueryExecutorRegistry m_instance = new QQueryExecutorRegistry();

	static public final QQueryExecutorRegistry getInstance() {
		return m_instance;
	}

	public synchronized int size() {
		return m_queryRendererList.size();
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
