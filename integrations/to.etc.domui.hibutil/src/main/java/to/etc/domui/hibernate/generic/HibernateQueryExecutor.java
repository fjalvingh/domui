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
public class HibernateQueryExecutor implements IQueryExecutor<BuggyHibernateBaseContext>, IQueryExecutorFactory {
	static public final IQueryExecutorFactory FACTORY = new HibernateQueryExecutor();

	protected HibernateQueryExecutor() {}

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
	public <T> T find(BuggyHibernateBaseContext root, ICriteriaTableDef<T> metatable, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> T getInstance(BuggyHibernateBaseContext root, ICriteriaTableDef<T> clz, Object pk) throws Exception {
		throw new IllegalStateException("Inapplicable call for " + getClass().getName());
	}

	@Override
	public <T> List<T> query(BuggyHibernateBaseContext root, QCriteria<T> q) throws Exception {
		Criteria crit = GenericHibernateHandler.createCriteria(root.getSession(), q); // Convert to Hibernate criteria
		return crit.list();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
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
