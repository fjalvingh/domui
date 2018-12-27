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
package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thingy which receives events from a table model. When a model changes it
 * must pass the changes on to it's listeners. The DataTable component for
 * instance registers itself as a listener to it's attached model. It uses
 * the events to re-draw the parts of the table that have changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 6, 2008
 */
public interface ITableModelListener<T> {
	/**
	 * Called after a row is added to the model.
	 */
	void rowAdded(@NonNull ITableModel<T> model, int index, @NonNull T value) throws Exception;

	/**
	 * Called after a row has been deleted.
	 */
	void rowDeleted(@NonNull ITableModel<T> model, int index, @NonNull T value) throws Exception;

	/**
	 * Called after a row has been changed.
	 */
	void rowModified(@NonNull ITableModel<T> model, int index, @NonNull T value) throws Exception;

	void rowsSorted(@NonNull ITableModel<T> model) throws Exception;

	/**
	 * Called when the entire content of the model changed. This should indicate a complete content
	 * redraw usually.
	 */
	void modelChanged(@Nullable ITableModel<T> model) throws Exception;
}
