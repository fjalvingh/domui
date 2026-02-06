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
package to.etc.domui.hibernate.model;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QCriteriaQueryBase;
import to.etc.webapp.query.QSelection;

/**
 * Thingy which helps translating generic database stuff to Hibernate specific
 * thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
final public class GenericHibernateHandler {
	private GenericHibernateHandler() {
		//-- empty
	}

	/**
	 * Translate generalized criteria to Hibernate criteria on a session.
	 */
	static public <T> Query<T> createSelectionQuery(Session ses, QCriteria<T> qc) {
		try {
			HibernateCriteriaBuilder critBuilder = ses.getCriteriaBuilder();
			JpaCriteriaQuery<T> query = critBuilder.createQuery(qc.getBaseClass());
			qc.visit(new CriteriaCreatingVisitor<>(ses, critBuilder, query, qc));
			Query<T> anotherUselessQuery = ses.createQuery(query);
			handlePagination(anotherUselessQuery, qc);
			return anotherUselessQuery;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x); // Cannot happen.
		}
	}

	private static <T> void handlePagination(Query<T> query, QCriteriaQueryBase<?, ?> qc) {
		//-- 2. Handle limits and start: applicable to root criterion only
		if(qc.getLimit() > 0) {
			query.setMaxResults(qc.getLimit());
		}
		if(qc.getStart() > 0) {
			query.setFirstResult(qc.getStart());
		}
		if(qc.getTimeout() > 0)
			query.setTimeout(qc.getTimeout());
	}

	static public <T> Query<Object[]> createSelectionQuery(Session ses, QSelection<T> qc) {
		try {
			HibernateCriteriaBuilder critBuilder = ses.getCriteriaBuilder();
			JpaCriteriaQuery<Object[]> query = critBuilder.createQuery(Object[].class);

			qc.visit(new CriteriaCreatingVisitor<>(ses, critBuilder, query, qc));
			Query<Object[]> anotherUselessQuery = ses.createQuery(query);
			handlePagination(anotherUselessQuery, qc);
			return anotherUselessQuery;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x); // Cannot happen.
		}
	}
}
