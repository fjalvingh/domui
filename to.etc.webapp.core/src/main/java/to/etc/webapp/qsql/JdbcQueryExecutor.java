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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.ICriteriaTableDef;
import to.etc.webapp.query.IQueryExecutor;
import to.etc.webapp.query.IQueryExecutorFactory;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QSelection;

import java.util.List;

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
	public IQueryExecutor< ? > findContextHandler(@NonNull QDataContext root, @NonNull Class< ? > clz) {
		if(clz != null && isJdbcQuery(clz))
			return this;
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(@NonNull QDataContext root, @NonNull ICriteriaTableDef< ? > tableMeta) {
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(@NonNull QDataContext root, @NonNull Object recordInstance) {
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
	public <T> T find(@NonNull QDataContext root, @NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		return JdbcQuery.find(root, clz, pk);
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull QDataContext root, @NonNull Class<T> clz, @NonNull Object pk) throws Exception {
		return JdbcQuery.getInstance(root, clz, pk);
	}

	@Override
	public <T> T find(@NonNull QDataContext root, @NonNull ICriteriaTableDef<T> metatable, @NonNull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public @NonNull <T> T getInstance(@NonNull QDataContext root, @NonNull ICriteriaTableDef<T> clz, @NonNull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public @NonNull <T> List<T> query(@NonNull QDataContext root, @NonNull QCriteria<T> q) throws Exception {
		return JdbcQuery.query(root, q);
	}

	@Override
	public @NonNull List<Object[]> query(@NonNull QDataContext root, @NonNull QSelection< ? > sel) throws Exception {
		return JdbcQuery.query(root, sel);
	}

	@Override
	public void refresh(@NonNull QDataContext root, @NonNull Object o) throws Exception {
	//-- Noop
	}

	@Override
	public void save(@NonNull QDataContext root, @NonNull Object o) throws Exception {
		throw new IllegalStateException("Save operation not implemented for QJdbc classes");
	}

	@Override
	public void delete(@NonNull QDataContext root, @NonNull Object o) throws Exception {
		throw new IllegalStateException("delete operation not implemented for QJdbc classes");
	}

	@Override
	public void attach(@NonNull QDataContext root, @NonNull Object o) throws Exception {
		throw new IllegalStateException("attach operation not implemented for QJdbc classes");
	}
}
