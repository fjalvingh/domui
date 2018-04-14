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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A QDataContext proxy which allows queries to be sent to multiple rendering/selecting implementations. It delegates
 * all query handling to the appropriate query handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2010
 */
abstract public class QAbstractDataContext implements QDataContext {

	@NonNull
	private List<IQDataContextListener> m_qDataContextListeners = new ArrayList<IQDataContextListener>();

	private QDataContextFactory m_contextFactory;

	protected QAbstractDataContext(QDataContextFactory contextFactory) {
		m_contextFactory = contextFactory;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Determine query route helper code.					*/
	/*--------------------------------------------------------------*/

	protected QQueryExecutorRegistry getHandlerFactory() {
		return getFactory().getQueryHandlerList();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Partial delegating QDataContext implementation.		*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public @NonNull QDataContextFactory getFactory() {
		return m_contextFactory;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#find(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(final @NonNull Class<T> clz, final @NonNull Object pk) throws Exception {
		return getHandlerFactory().getHandler(this, clz).find(this, clz, pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#get(java.lang.Class, java.lang.Object)
	 */
	@Override
	public @NonNull
	<T> T get(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		T res = find(clz, pk);
		if(res == null) {
			throw new QNotFoundException(clz, pk);
		}
		return res;
	}

	@Override
	public <T> T find(@NonNull ICriteriaTableDef<T> metatable, @NonNull Object pk) throws Exception {
		return getHandlerFactory().getHandler(this, metatable).find(this, metatable, pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#getInstance(java.lang.Class, java.lang.Object)
	 */
	@Override
	public @NonNull <T> T getInstance(@NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		return getHandlerFactory().getHandler(this, clz).getInstance(this, clz, pk);
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull ICriteriaTableDef<T> metatable, @NonNull Object pk) throws Exception {
		return getHandlerFactory().getHandler(this, metatable).getInstance(this, metatable, pk);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#query(to.etc.webapp.query.QCriteria)
	 */
	@Override
	public @NonNull <T> List<T> query(final @NonNull QCriteria<T> q) throws Exception {
		getFactory().getEventListeners().callOnBeforeQuery(this, q);
		return getHandlerFactory().getHandler(this, q).query(this, q);
	}

	/**
	 * {@inheritDoc}
	 *
	 * This overrides the behaviour of Hibernate where a single-column selection does not
	 * return an array but that single object, for consistency's sake. It is slightly more
	 * expensive because an Object[1] is needed for every row, but compared with the heaps
	 * of memory Hibernate is already wasting this is peanuts.
	 *
	 * @see to.etc.webapp.query.QDataContext#query(to.etc.webapp.query.QSelection)
	 */
	@Override
	public @NonNull List<Object[]> query(@NonNull QSelection< ? > sel) throws Exception {
		getFactory().getEventListeners().callOnBeforeQuery(this, sel);
		return getHandlerFactory().getHandler(this, sel).query(this, sel);
	}

	@Override
	@NonNull
	public <R> List<R> query(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionQuery(this, resultInterface, sel);
	}

	@Override
	@Nullable
	public <R> R queryOne(@NonNull Class<R> resultInterface, @NonNull QSelection< ? > sel) throws Exception {
		return QQueryUtils.mapSelectionOneQuery(this, resultInterface, sel);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#queryOne(to.etc.webapp.query.QCriteria)
	 */
	@Override
	public <T> T queryOne(final @NonNull QCriteria<T> q) throws Exception {
		List<T> res = query(q);
		if(res.size() == 0)
			return null;
		if(res.size() > 1)
			throw new QTooManyResultsException(q, res.size());
				//"The criteria-query " + q + " returns " + res.size() + " results instead of one");
		return res.get(0);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#queryOne(to.etc.webapp.query.QCriteria)
	 */
	@Override
	public Object[] queryOne(final @NonNull QSelection< ? > sel) throws Exception {
		List<Object[]> res = query(sel);
		if(res.size() == 0)
			return null;
		if(res.size() > 1)
			throw new QTooManyResultsException(sel, res.size());
			//throw new IllegalStateException("The criteria-query " + sel + " returns " + res.size() + " results instead of one");
		return res.get(0);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#attach(java.lang.Object)
	 */
	@Override
	public void attach(final @NonNull Object o) throws Exception {
		getHandlerFactory().getHandler(this, o).attach(this, o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#delete(java.lang.Object)
	 */
	@Override
	public void delete(final @NonNull Object o) throws Exception {
		getHandlerFactory().getHandler(this, o).delete(this, o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#save(java.lang.Object)
	 */
	@Override
	public void save(final @NonNull Object o) throws Exception {
		getHandlerFactory().getHandler(this, o).save(this, o);
		if(o instanceof IIdentifyable) {
			for(IQDataContextListener icl : m_qDataContextListeners) {
				icl.instanceSaved((IIdentifyable< ? >) o);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(final @NonNull Object o) throws Exception {
		getHandlerFactory().getHandler(this, o).refresh(this, o);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContext#addListener(to.etc.webapp.query.IQDataContextListener)
	 */
	@Override
	public void addListener(@NonNull IQDataContextListener qDataContextListener) {
		m_qDataContextListeners.add(qDataContextListener);
	}
}
