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
package to.etc.domui.component.form;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Accepts both enum and bools and shows a combobox with the possible choices.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryEnumAndBool implements ControlFactory {
	/**
	 * Accept boolean, Boolean and Enum.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(final @Nonnull PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboFixed.class)) // This one only creates ComboFixed thingies
			return -1;
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	/**
	 * Create and init a ComboFixed combobox.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public @Nonnull <T> ControlFactoryResult createControl(final @Nonnull IReadOnlyModel< ? > model, final @Nonnull PropertyMetaModel<T> pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		//-- FIXME EXPERIMENTAL use a DisplayValue control to present the value instead of a horrible disabled combobox
		if(!editable && controlClass == null) {
			DisplayValue<T> dv = new DisplayValue<T>(pmm.getActualType());
			dv.defineFrom(pmm);
			return new ControlFactoryResult(dv, model, pmm);
		}

		ComboFixed<T> c = (ComboFixed<T>) DomApplication.get().getControlBuilder().createComboFor(pmm, editable);

		//		// Create a domainvalued combobox by default.
		//		Object[] vals = pmm.getDomainValues();
		//		ClassMetaModel ecmm = null;
		//		List<ComboFixed.Pair<Object>> vl = new ArrayList<ComboFixed.Pair<Object>>();
		//		for(Object o : vals) {
		//			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
		//			if(label == null) {
		//				if(ecmm == null)
		//					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
		//				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
		//				if(label == null)
		//					label = o == null ? "" : o.toString();
		//			}
		//			vl.add(new ComboFixed.Pair<Object>(o, label));
		//		}
		//
		//		ComboFixed< ? > c = new ComboFixed<Object>(vl);
		//		if(pmm.isRequired())
		//			c.setMandatory(true);
		//		if(!editable || pmm.getReadOnly() == YesNoType.YES)
		//			c.setDisabled(true);
		//		String s = pmm.getDefaultHint();
		//		if(s != null)
		//			c.setTitle(s);
		return new ControlFactoryResult(c, model, pmm);
	}
}
