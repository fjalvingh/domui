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

import java.math.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.util.*;

/**
 * Factory which creates a Text input specialized for entering monetary amounts. This
 * accepts properties with type=Double/double or BigDecimal, and with one of the monetary
 * numeric presentations.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 4, 2009
 */
public class ControlFactoryMoney implements ControlFactory {
	/**
	 * Accept any type using a string.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel)
	 */
	@Override
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(Text.class)) // This will create a Text class,
			return -1;
		Class<?> clz = pmm.getActualType();
		if(clz != Double.class && clz != double.class && clz != BigDecimal.class) // Must be proper type
			return -1;
		if(!NumericPresentation.isMonetary(pmm.getNumericPresentation()))
			return -1;
		return 2;
	}

	/**
	 * Create a Text control with the basic monetary converter, or the proper converter for the specified type.
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public ControlFactoryResult createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		Class< ? > iclz = pmm.getActualType();

		if(!editable) {
			/*
			 * 20100302 vmijic - added DisplayValue as money readonly presentation
			 * FIXME EXPERIMENTAL: replace the code below (which is still fully available) with the
			 * display-only component.
			 */
			DisplayValue dv = new DisplayValue(iclz);
			//			dv.setTextAlign(TextAlign.RIGHT);
			dv.addCssClass("ui-numeric");
			UIControlUtil.assignMonetaryConverter(pmm, editable, dv);
			String s = pmm.getDefaultHint();
			if(s != null)
				dv.setTitle(s);
			return new ControlFactoryResult(dv, model, pmm);
		}

		Text<?> txt;
		if(pmm.getActualType() == Double.class || pmm.getActualType() == double.class) {
			txt = UIControlUtil.createDoubleMoneyInput(pmm, editable);
		} else if(pmm.getActualType() == BigDecimal.class) {
			txt = UIControlUtil.createBDMoneyInput(pmm, editable);
		} else
				throw new IllegalStateException("Cannot handle type=" + pmm.getActualType() + " in monetary control factory");
		return new ControlFactoryResult(txt, model, pmm);
	}
}
