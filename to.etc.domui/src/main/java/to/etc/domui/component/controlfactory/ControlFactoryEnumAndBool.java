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
package to.etc.domui.component.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.ComboFixed;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.DisplayValue;
import to.etc.domui.server.DomApplication;

/**
 * Accepts both enum and bools and shows a combobox with the possible choices.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryEnumAndBool implements PropertyControlFactory {
	/**
	 * Accept boolean, Boolean and Enum.
	 *
	 * @see to.etc.domui.component.controlfactory.PropertyControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(final @NonNull PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboFixed.class)) // This one only creates ComboFixed thingies
			return -1;
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 10 : 0;
	}

	/**
	 * Create and init a ComboFixed combobox.
	 *
	 * @see to.etc.domui.component.controlfactory.PropertyControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public @NonNull <T> ControlFactoryResult createControl(final @NonNull PropertyMetaModel<T> pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		//-- FIXME EXPERIMENTAL use a DisplayValue control to present the value instead of a horrible disabled combobox
		if(!editable && controlClass == null) {
			DisplayValue<T> dv = new DisplayValue<T>(pmm.getActualType());
			dv.defineFrom(pmm);
			return new ControlFactoryResult(dv);
		}

		ComboFixed<T> c = (ComboFixed<T>) DomApplication.get().getControlBuilder().createComboFor(pmm, editable);
		return new ControlFactoryResult(c);
	}
}
