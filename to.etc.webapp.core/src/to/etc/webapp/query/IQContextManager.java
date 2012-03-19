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
 * Interface for a QContextManager handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 17, 2009
 */
public interface IQContextManager {
	/**
	 * Initialize the data context factory that is to be used by default to allocate QDataContexts. Can be called
	 * only once.
	 * @param f
	 */
	void setContextFactory(@Nonnull QDataContextFactory f);

	/**
	 * Return the default QDataContextFactory. This is the root of *all* default connections
	 * allocated through DomUI.
	 *
	 * @return
	 */
	@Nonnull
	QDataContextFactory getDataContextFactory();

	/**
	 * Create an unmanaged (manually closed) context factory.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	QDataContext createUnmanagedContext() throws Exception;

	/**
	 * Get/create a shared context factory. The context factory gets attached
	 * to the container it is shared in, and will always try to re-use any
	 * QDataContext already present in the container. In addition, all data contexts
	 * allocated thru this mechanism have a disabled close() method, preventing
	 * them from closing the shared connection.
	 *
	 * @param cc
	 * @return
	 */
	@Nonnull
	QDataContextFactory getSharedContextFactory(@Nonnull IQContextContainer cc);

	/**
	 * Gets a shared QDataContext from the container. If it is not already present it
	 * will be allocated, stored in the container for later reuse and returned. The context
	 * is special in that it cannot be closed() using it's close() call - it is silently
	 * ignored.
	 */
	@Nonnull
	QDataContext getSharedContext(@Nonnull IQContextContainer cc) throws Exception;

	/**
	 * If the specified container contains a shared context close it.
	 * @param cc
	 */
	void closeSharedContext(@Nonnull IQContextContainer cc);
}
