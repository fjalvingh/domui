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
package to.etc.domui.component2.controlfactory;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component2.combo.*;
import to.etc.domui.dom.html.*;

/**
 * Accepts both enum and bools and shows a combobox with the possible choices.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlCreatorEnumAndBool implements IControlCreator {
	/**
	 * Accept boolean, Boolean and Enum.
	 *
	 * @see to.etc.domui.component.controlfactory.PropertyControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public <T> int accepts(PropertyMetaModel<T> pmm, Class< ? extends IControl<T>> controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboFixed2.class)) // This one only creates ComboFixed2 thingies
			return -1;
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	/**
	 * Create and init a ComboFixed combobox.
	 *
	 * @see to.etc.domui.component.controlfactory.PropertyControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public <T, C extends IControl<T>> C createControl(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		ComboFixed2< ? > c = ComboFixed2.createComboFor(pmm, true);
		if(pmm.getActualType() == boolean.class)
			c.setMandatory(true);

		return (C) c;
	}
}
