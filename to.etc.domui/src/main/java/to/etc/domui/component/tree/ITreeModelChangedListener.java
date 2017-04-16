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
package to.etc.domui.component.tree;

import javax.annotation.*;

@DefaultNonNull
public interface ITreeModelChangedListener<T> {

	/**
	 * Called after a node is added in the model
	 * @param parent the parent of the added node
	 * @param index the index of the added node
	 * @param node the added node
	 * @throws Exception
	 */
	void onNodeAdded(@Nullable T parent, int index, T node) throws Exception;

	/**
	 * Called after a node is removed in the model
	 * @param oldParent the parent of the removed node
	 * @param oldIndex the index of the node that was removed
	 * @param deletedNode The node that was just deleted
	 *
	 * @throws Exception
	 */
	void onNodeRemoved(@Nullable T oldParent, int oldIndex, T deletedNode) throws Exception;

	/**
	 * Called after a node is updated in the model.
	 * @param node the node that is updated
	 * @throws Exception
	 */
	void onNodeUpdated(T node) throws Exception;

}
