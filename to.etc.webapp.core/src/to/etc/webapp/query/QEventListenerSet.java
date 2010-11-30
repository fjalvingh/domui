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
 * Used to separate event registration from the data source. An instance of
 * this can be shared by multiple QDataContext factories so that they all share
 * the same registered event handlers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class QEventListenerSet {
	static public final QEventListenerSet	EMPTY_SET = new QEventListenerSet(Collections.unmodifiableList(new ArrayList<IQueryListener>()));

	private List<IQueryListener>		m_listeners = Collections.EMPTY_LIST;

	public QEventListenerSet() {
	}

	private QEventListenerSet(List<IQueryListener> l) {
		m_listeners = l;
	}

	/**
	 * Add a new listener for queries for this source. All data sources obtained
	 * from this source will use these listeners.
	 * @param l
	 */
	synchronized public void addQueryListener(IQueryListener l) {
		if(m_listeners.contains(l))
			return;
		if(m_listeners == Collections.EMPTY_LIST)
			m_listeners = new ArrayList<IQueryListener>(1);
		else
			m_listeners = new ArrayList<IQueryListener>(m_listeners);
		m_listeners.add(l);
	}

	/**
	 * Remove an earlier-registered query listener.
	 * @param l
	 */
	synchronized public void removeQueryListener(IQueryListener l) {
		m_listeners = new ArrayList<IQueryListener>(m_listeners);
		m_listeners.remove(l);
	}

	/**
	 * Return an iterator over all registered event listeners.
	 * @return
	 */
	synchronized public Iterable<IQueryListener>	getListenerIterator() {
		return m_listeners;
	}

	/**
	 * Calls all listeners in order.
	 * @param qc
	 */
	public void		callOnBeforeQuery(QDataContext dc, QCriteriaQueryBase<?> qc) throws Exception {
		for(IQueryListener l: getListenerIterator())
			l.onBeforeQuery(dc, qc);
	}
}
