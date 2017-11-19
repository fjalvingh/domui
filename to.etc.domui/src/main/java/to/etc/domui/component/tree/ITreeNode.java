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

/**
 * Can be used to implement whatever's needed for a ITreeModel.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public interface ITreeNode<T extends ITreeNode<T>> {
	/**
	 * If possible this should quickly decide if this node has children or not. This is
	 * used to render an expanded node's state icons. If determining whether a node has
	 * children is an expensive option this method should return TRUE always; this causes
	 * the state icon to display as if children are available and the user has the possibility
	 * to expand that node. At that time we'll call getChildCount() which <i>must</i> determine
	 * the #of children. If that returns zero it will at that time properly re-render the state
	 * of the node, showing that the node is actually a leaf and cannot be expanded further.
	 * @param item
	 * @return
	 */
	boolean hasChildren() throws Exception;

	/**
	 * Returns the #of children for this object. This must return the actual number.
	 * @param item
	 * @return
	 */
	int getChildCount() throws Exception;

	/**
	 * Returns the nth child in the parent's list.
	 * @param parent
	 * @param index
	 * @return
	 * @throws Exception
	 */
	T getChild(int index) throws Exception;

	T getParent() throws Exception;
}
