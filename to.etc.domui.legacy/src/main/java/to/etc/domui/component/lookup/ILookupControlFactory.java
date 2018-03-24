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
package to.etc.domui.component.lookup;

import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.dom.html.IControl;

import javax.annotation.Nonnull;

/**
 * Creates the stuff needed to generate a single property lookup control, plus
 * the stuff to handle the control's input and converting it to part of a
 * QCriteria restriction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 23, 2008
 */
@Deprecated
public interface ILookupControlFactory {
	/**
	 * Returns >=0 if this can create a lookup instance for a property.
	 * @param pmm
	 * @return
	 */
	<T, X extends IControl<T>> int accepts(@Nonnull SearchPropertyMetaModel pmm, X control);

	<T, X extends IControl<T>> ILookupControlInstance<?> createControl(@Nonnull SearchPropertyMetaModel spm, X control);
}
