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

import javax.annotation.*;


/**
 * A thingy which knows how to get a QDataContext to access the database. This
 * usually returns a shared context: the one used by the current request.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public interface QDataContextFactory extends IQDataContextSource {
	/**
	 * Get the current Session to use for querying.
	 * @return
	 * @throws Exception
	 */
	@Override
	@Nonnull
	QDataContext getDataContext() throws Exception;

	/**
	 * Returns all event listeners that need to be called for queries executed by contexts generated from here.
	 * @return
	 */
	@Nonnull
	QEventListenerSet		getEventListeners();

	/**
	 * Returns handlers for all query types.
	 * @return
	 */
	@Nonnull
	QQueryExecutorRegistry getQueryHandlerList();
}
