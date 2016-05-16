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

/**
 * When implemented by a TableModel, methods herein will be called when
 * sorting is required.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 19, 2008
 */
public interface ISortableTableModel {
	/**
	 *
	 * @param key
	 * @param descending
	 * @throws Exception
	 */
	void sortOn(String key, boolean descending) throws Exception;

	/**
	 * DO NOT USE: Whomever uses the model should define sorting and remember it.
	 *
	 * If this model is currently sorted, this returns an identifier (usually a
	 * property reference) indicating on which column the thingy is sorted. If
	 * the model is unsorted this returns null.
	 * @return
	 */
	@Deprecated
	String getSortKey();

	/**
	 * DO NOT USE: Whomever uses the model should define sorting and remember it.
	 * If the set is a sorted set, this returns TRUE if the sort order is
	 * descending. The return value is <b>undefined</b> for an unsorted model.
	 * @return
	 */
	@Deprecated
	boolean isSortDescending();
}
