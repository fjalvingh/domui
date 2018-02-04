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
package to.etc.domui.component.searchpanel.lookupcontrols;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
final class StringLookupFactory2<T> implements ILookupFactory<T> {
	@Nonnull @Override public FactoryPair<T> createControl(@Nonnull SearchPropertyMetaModel spm) {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) spm.getProperty();
		Text2<T> txt = createControl(pmm);

		int size = MetaManager.calculateTextSize(pmm);
		if(size > 0)
			txt.setSize(size);

		if(pmm.getLength() > 0)
			txt.setMaxLength(pmm.getLength());
		String hint = MetaUtils.findHintText(spm);
		if(hint != null)
			txt.setTitle(hint);

		return new FactoryPair<T>(new ObjectLookupQueryBuilder<>(pmm.getName()), txt);
	}

	private Text2<T> createControl(PropertyMetaModel<T> pmm) {
		Class<T> iclz = pmm.getActualType();

		//-- Boolean/boolean types? These need a tri-state checkbox
		if(iclz == Boolean.class || iclz == Boolean.TYPE) {
			throw new IllegalStateException("I need a tri-state checkbox component to handle boolean lookup thingies.");
		}

		Text2<T> ctl = new Text2<>(iclz);
		if(pmm.getConverter() != null)
			ctl.setConverter(pmm.getConverter());
		return ctl;
	}
}
