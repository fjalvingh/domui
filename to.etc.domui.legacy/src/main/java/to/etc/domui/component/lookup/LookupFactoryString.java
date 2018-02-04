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

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;

@Deprecated
@SuppressWarnings("unchecked") final class LookupFactoryString implements ILookupControlFactory {
	@Override
	public <T, X extends IControl<T>> int accepts(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		if(control != null) {
			if(!(control instanceof Text2<?>))
				return -1;
			Text2<?> t = (Text2<?>) control;
			if(t.getActualType() != String.class)
				return -1;
		}
		return 1; // Accept all properties (will fail on incompatible ones @ input time)
	}

	@Override
	public <T, X extends IControl<T>> ILookupControlInstance<T> createControl(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) spm.getProperty();
		Class<T> iclz = pmm.getActualType();

		//-- Boolean/boolean types? These need a tri-state checkbox
		if(iclz == Boolean.class || iclz == Boolean.TYPE) {
			throw new IllegalStateException("I need a tri-state checkbox component to handle boolean lookup thingies.");
		}

		//-- Treat everything else as a String using a converter.
		final Text2<T> txt = new Text2<T>(iclz);
		int size = MetaManager.calculateTextSize(pmm);
		if(size > 0)
			txt.setSize(size);

		if(pmm.getConverter() != null)
			txt.setConverter(pmm.getConverter());
		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		String hint = MetaUtils.findHintText(spm);
		if(hint != null)
			txt.setTitle(hint);

		//-- Converter thingy is known. Now add a
		return new BaseAbstractLookupControlImpl<T>(txt) {
			@Override
			public @Nonnull AppendCriteriaResult appendCriteria(@Nonnull QCriteria<?> crit) throws Exception {
				Object value = null;
				try {
					value = txt.getValue();
				} catch(Exception x) {
					return AppendCriteriaResult.INVALID; // Has validation error -> exit.
				}
				if(value == null || (value instanceof String && ((String) value).trim().length() == 0))
					return AppendCriteriaResult.EMPTY; // Is okay but has no data

				// FIXME Handle minimal-size restrictions on input (search field metadata


				//-- Put the value into the criteria..
				if(value instanceof String) {
					String str = (String) value;
					str = str.trim().replace("*", "%") + "%";
					crit.ilike(pmm.getName(), str);
				} else {
					crit.eq(pmm.getName(), value); // property == value
				}
				return AppendCriteriaResult.VALID;
			}

			@Override
			public T getValue() {
				return txt.getValue();
			}

			@Override
			public void setValue(T value) {
				txt.setValue(value);
			}
		};
	}
}
