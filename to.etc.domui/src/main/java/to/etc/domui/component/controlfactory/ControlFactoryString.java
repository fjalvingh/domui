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

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.DisplayValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * This is a fallback factory; it accepts anything and returns a {@link Text2} component for
 * it. It has no separate control for non editable, as each control must handle that properly
 * by itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 5, 2017
 */
@SuppressWarnings("unchecked")
public class ControlFactoryString implements PropertyControlFactory {
	/**
	 * Accept any type using a string.
	 */
	@Override
	public int accepts(final @Nonnull PropertyMetaModel< ? > pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		if(controlClass != null) {
			if(!controlClass.isAssignableFrom(Text2.class) && !controlClass.isAssignableFrom(DisplayValue.class))
				return -1;
		}
		return 2;
	}

	@Override
	public @Nonnull <T> ControlFactoryResult createControl(final @Nonnull PropertyMetaModel<T> pmm, final boolean editable, @Nullable Class< ? > controlClass) {
		Class<T> iclz = pmm.getActualType();
		//if(!editable) {
		//	/*
		//	 * FIXME EXPERIMENTAL: replace the code below (which is still fully available) with the
		//	 * display-only component.
		//	 */
		//	DisplayValue<T> dv = new DisplayValue<T>(iclz);
		//	if(pmm.getConverter() != null)
		//		dv.setConverter(pmm.getConverter());
		//	String s = pmm.getDefaultHint();
		//	if(s != null)
		//		dv.setTitle(s);
		//	return new ControlFactoryResult(dv);
		//}

		Text2<T> txt;

		if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class || pmm.getActualType() == BigDecimal.class) {
			txt = (Text2<T>) Text2.createNumericInput((PropertyMetaModel<Double>) pmm, editable);
		} else {
			txt = Text2.createText(iclz, pmm, editable);
		}
		return new ControlFactoryResult(txt);
	}
}
