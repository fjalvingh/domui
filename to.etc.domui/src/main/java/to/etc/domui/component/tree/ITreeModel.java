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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The base model for a Tree. This encapsulates the knowledge about a tree, and returns tree-based
 * context information for when the tree is being built.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public interface ITreeModel<T> {
	/**
	 * Returns the #of children for this object. This must return the actual number.
	 */
	int getChildCount(@NonNull T item) throws Exception;

	/**
	 * If possible this should quickly decide if a tree node has children or not. This is
	 * used to render an expanded node's state icons. If determining whether a node has
	 * children is an expensive option this method should return TRUE always; this causes
	 * the state icon to display as if children are available and the user has the possibility
	 * to expand that node. At that time we'll call getChildCount() which <i>must</i> determine
	 * the #of children. If that returns zero it will at that time properly re-render the state
	 * of the node, showing that the node is actually a leaf and cannot be expanded further.
	 */
	default boolean hasChildren(@NonNull T item) throws Exception {
		return getChildCount(item) != 0;
	}

	/**
	 * Get the root object of the tree.
	 */
	@NonNull
	T getRoot() throws Exception;

	/**
	 * Returns the nth child in the parent's list. This call can do actual expansion the first time it's called
	 * when a tree is lazily-loaded.
	 */
	@NonNull
	T getChild(@NonNull T parent, int index) throws Exception;

	/**
	 * Get the parent node of a child in the tree. This may only return null for the root node.
	 */
	@Nullable
	T getParent(@NonNull T child) throws Exception;

	/**
	 * Add a listener to be called when nodes on the tree change.
	 */
	default void addChangeListener(@NonNull ITreeModelChangedListener<T> l) {}

	/**
	 * Remove a registered change listener. Fails silently when the listener was not registered at all.
	 */
	default void removeChangeListener(@NonNull ITreeModelChangedListener<T> l) {}

	/**
	 * Called when this node is attempted to be expanded. This call can be used to refresh/lazily load the
	 * children of the passed node. This call is issued <i>every time</i> this node's tree is expanded so
	 * take care to only reload when needed.
	 */
	default void expandChildren(@NonNull T item) throws Exception {
	}

	/**
	 * Called when this node's children are to be collapsed. This call is executed for every node that
	 * was expanded but is collapsed. It can be used to release resources for collapsed nodes.
	 */
	default void collapseChildren(@NonNull T item) throws Exception {
	}

	/**
	 * Calculates a tree path for a given node, as a set of nodes that walk to the item. The
	 * root element is always the 1st element in the treepath
	 */
	@NonNull
	default List<T> getTreePath(@NonNull T item) throws Exception {
		List<T> path = new ArrayList<>();
		addParentPath(path, item);
		return path;
	}

	default void addParentPath(@NonNull List<T> path, @NonNull T item) throws Exception {
		T parent = getParent(item);

		/*
		 * jal 20081127 The explicit compare with the root node is needed because we allow the root
		 * node to be null. In that case the path to the item MUST start with null (representing the
		 * root node).
		 */
		if(parent == getRoot()) {
			path.add(parent);
		} else if(parent != null) {
			addParentPath(path, parent);
		}
		path.add(item);
	}

}
