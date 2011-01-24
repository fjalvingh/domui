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

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Accepts the "java.util.Date" type only and creates a DateInput component for it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryDate implements ControlFactory {
	/**
	 * Accept java.util.Date class <i>only</i>.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(final PropertyMetaModel< ? > pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(DateInput.class))
			return -1;

		Class< ? > iclz = pmm.getActualType();
		if(Date.class.isAssignableFrom(iclz)) {
			return 2;
		}
		return 0;
	}

	@Override
	public <T> ControlFactoryResult createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel<T> pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(!editable && (controlClass == null || controlClass.isAssignableFrom(Text.class))) {
			//			Text<Date> txt = new Text<Date>(Date.class);
			//			txt.setReadOnly(true);
			// FIXME EXPERIMENTAL Replace the TEXT control with a DisplayValue control.
			DisplayValue<Date> txt = new DisplayValue<Date>(Date.class);

			//20100208 vmijic - fixed readonly presentation for date fields.
			Class< ? extends IConverter<Date>> cc;
			if(pmm == null)
				cc = DateTimeConverter.class;
			else {
				switch(pmm.getTemporal()){
					default:
						throw new IllegalStateException("Unsupported temporal metadata type: " + pmm.getTemporal());
					case UNKNOWN:
						/*$FALL_THROUGH$*/
					case DATETIME:
						cc = DateTimeConverter.class;
						break;
					case DATE:
						cc = DateConverter.class;
						break;
					case TIME:
						cc = TimeOnlyConverter.class;
						break;
				}
			}

			txt.setConverter(ConverterRegistry.getConverterInstance(cc));
			return new ControlFactoryResult(txt, model, (PropertyMetaModel<Date>) pmm);
		}

		DateInput di = new DateInput();
		if(pmm.isRequired())
			di.setMandatory(true);
		if(!editable)
			di.setDisabled(true);
		if(pmm.getTemporal() == TemporalPresentationType.DATETIME)
			di.setWithTime(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			di.setTitle(s);
		return new ControlFactoryResult(di, model, (PropertyMetaModel<Date>) pmm);
	}
}
