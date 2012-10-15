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
package to.etc.domui.component.ntbl;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;

/**
 * Event handler for row-based editors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 21, 2009
 */
public interface IRowEditorEvent<T, E extends NodeContainer> {
	/**
	 * Called after a row has been edited in an editable table component, when editing is (somehow) marked
	 * as complete. When called the editor's contents has been moved to the model by using the bindings. This method
	 * can be used to check the data for validity or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @param tablecomponent
	 * @param editor
	 * @param instance
	 * @param isNew true when inserting row is changed, false when editing row is changed
	 * @return false to refuse the change.
	 * @throws Exception
	 */
	boolean onRowChanged(@Nonnull TableModelTableBase<T> tablecomponent, @Nonnull E editor, @Nonnull T instance, boolean isNew) throws Exception;
}
