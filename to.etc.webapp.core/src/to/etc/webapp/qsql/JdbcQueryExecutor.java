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
package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

public class JdbcQueryExecutor implements IQueryExecutor<QDataContext>, IQueryExecutorFactory {
	static public final JdbcQueryExecutor FACTORY = new JdbcQueryExecutor();

	protected JdbcQueryExecutor() {}

	/**
	 * FIXME Determine if this is a JDBC query.
	 * @param clz
	 * @return
	 */
	static private boolean isJdbcQuery(Class< ? > clz) {
		return clz.getAnnotation(QJdbcTable.class) != null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IQueryHandlerFactory impl.							*/
	/*--------------------------------------------------------------*/
	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, Class< ? > clz) {
		if(clz != null && isJdbcQuery(clz))
			return this;
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, ICriteriaTableDef< ? > tableMeta) {
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(QDataContext root, Object recordInstance) {
		if(recordInstance == null || !isJdbcQuery(recordInstance.getClass()))
			return null;
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAbstractQueryHandler impl.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.webapp.query.IQueryExecutor#find(to.etc.webapp.query.QDataContext, java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(QDataContext root, Class<T> clz, Object pk) throws Exception {
		return JdbcQuery.find(root, clz, pk);
	}

	@Override
	public <T> T getInstance(QDataContext root, Class<T> clz, Object pk) throws Exception {
		return JdbcQuery.getInstance(root, clz, pk);
	}

	@Override
	public <T> T find(QDataContext root, ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> T getInstance(QDataContext root, ICriteriaTableDef<T> clz, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> List<T> query(QDataContext root, QCriteria<T> q) throws Exception {
		return JdbcQuery.query(root, q);
	}

	@Override
	public List<Object[]> query(QDataContext root, QSelection< ? > sel) throws Exception {
		return JdbcQuery.query(root, sel);
	}

	@Override
	public void refresh(QDataContext root, Object o) throws Exception {
	//-- Noop
	}

	@Override
	public void save(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("Save operation not implemented for QJdbc classes");
	}

	@Override
	public void delete(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("delete operation not implemented for QJdbc classes");
	}

	@Override
	public void attach(QDataContext root, Object o) throws Exception {
		throw new IllegalStateException("attach operation not implemented for QJdbc classes");
	}
}
