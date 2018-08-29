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

import org.hibernate.*;

import to.etc.webapp.query.*;

/**
 * Thingy which helps translating generic database stuff to Hibernate specific
 * thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class GenericHibernateHandler {
	/**
	 * Translate generalized criteria to Hibernate criteria on a session.
	 *
	 * @param ses
	 * @param qc
	 * @return
	 */
	static public Criteria createCriteria(Session ses, QCriteria< ? > qc) {
		try {
			Criteria c = ses.createCriteria(qc.getBaseClass(), "base");
			qc.visit(new CriteriaCreatingVisitor(ses, c));
			return c;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x); // Cannot happen.
		}
	}
	static public Criteria createCriteria(Session ses, QSelection< ? > qc) {
		try {
			Criteria c = ses.createCriteria(qc.getBaseClass(), "base");
			qc.visit(new CriteriaCreatingVisitor(ses, c));
			return c;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x); // Cannot happen.
		}
	}
}
