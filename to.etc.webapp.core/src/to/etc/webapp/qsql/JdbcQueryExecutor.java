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

import javax.annotation.*;

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
	public IQueryExecutor< ? > findContextHandler(@Nonnull QDataContext root, @Nonnull Class< ? > clz) {
		if(clz != null && isJdbcQuery(clz))
			return this;
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(@Nonnull QDataContext root, @Nonnull ICriteriaTableDef< ? > tableMeta) {
		return null;
	}

	@Override
	public IQueryExecutor< ? > findContextHandler(@Nonnull QDataContext root, @Nonnull Object recordInstance) {
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
	public <T> T find(@Nonnull QDataContext root, @Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		return JdbcQuery.find(root, clz, pk);
	}

	@Override
	public @Nonnull <T> T getInstance(@Nonnull QDataContext root, @Nonnull Class<T> clz, @Nonnull Object pk) throws Exception {
		return JdbcQuery.getInstance(root, clz, pk);
	}

	@Override
	public <T> T find(@Nonnull QDataContext root, @Nonnull ICriteriaTableDef<T> metatable, @Nonnull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public @Nonnull <T> T getInstance(@Nonnull QDataContext root, @Nonnull ICriteriaTableDef<T> clz, @Nonnull Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public @Nonnull <T> List<T> query(@Nonnull QDataContext root, @Nonnull QCriteria<T> q) throws Exception {
		return JdbcQuery.query(root, q);
	}

	@Override
	public @Nonnull List<Object[]> query(@Nonnull QDataContext root, @Nonnull QSelection< ? > sel) throws Exception {
		return JdbcQuery.query(root, sel);
	}

	@Override
	public void refresh(@Nonnull QDataContext root, @Nonnull Object o) throws Exception {
	//-- Noop
	}

	@Override
	public void save(@Nonnull QDataContext root, @Nonnull Object o) throws Exception {
		throw new IllegalStateException("Save operation not implemented for QJdbc classes");
	}

	@Override
	public void delete(@Nonnull QDataContext root, @Nonnull Object o) throws Exception {
		throw new IllegalStateException("delete operation not implemented for QJdbc classes");
	}

	@Override
	public void attach(@Nonnull QDataContext root, @Nonnull Object o) throws Exception {
		throw new IllegalStateException("attach operation not implemented for QJdbc classes");
	}
}
