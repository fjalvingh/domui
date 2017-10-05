/*
 * DomUI Java User Interface library
 * Copyright (c) 2017 by Frits Jalvingh.
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://github.com/fjalvingh/domui
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.controlfactory;

import to.etc.domui.component.input.Text;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.DisplayValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * This is a fallback factory; it accepts anything and shows a String edit component OR a
 * DisplayValue component for it. It hopes that the control can convert the string input
 * value to the actual type using the registered Converters. This is also the factory
 * for regular Strings.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")
public class ControlFactoryStringOld implements PropertyControlFactory {
	/**
	 * Accept any type using a string.
	 */
	@Override
	public int accepts(final @Nonnull PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		if(controlClass != null) {
			if(!controlClass.isAssignableFrom(Text.class) && !controlClass.isAssignableFrom(DisplayValue.class))
				return -1;
		}

		return 1;
	}

	@Override
	public @Nonnull <T> ControlFactoryResult createControl(final @Nonnull PropertyMetaModel<T> pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		Class<T> iclz = pmm.getActualType();
		if(!editable) {
			/*
			 * FIXME EXPERIMENTAL: replace the code below (which is still fully available) with the
			 * display-only component.
			 */
			DisplayValue<T> dv = new DisplayValue<T>(iclz);
			if(pmm.getConverter() != null)
				dv.setConverter(pmm.getConverter());
			String s = pmm.getDefaultHint();
			if(s != null)
				dv.setTitle(s);
			return new ControlFactoryResult(dv);
		}

		Text<T> txt;

		if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class || pmm.getActualType() == BigDecimal.class) {
			txt = (Text<T>) Text.createNumericInput((PropertyMetaModel<Double>) pmm, editable);
		} else {
			txt = Text.createText(iclz, pmm, editable);
		}
		return new ControlFactoryResult(txt);
	}
}
