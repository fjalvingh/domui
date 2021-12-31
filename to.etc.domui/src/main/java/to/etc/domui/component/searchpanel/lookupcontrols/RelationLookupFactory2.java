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

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component2.lookupinput.LookupInput2;

/**
 * Creates a {@link to.etc.domui.component2.lookupinput.LookupInput2} which allows lookup of the
 * related data record.
 */
final class RelationLookupFactory2<Q, D> implements ILookupFactory<Q, D> {
	@NonNull
	@Override
	public FactoryPair<Q, D> createControl(@NonNull SearchPropertyMetaModel spm) {
		PropertyMetaModel<?> pmm = spm.getProperty();
		LookupInput2<D> control = new LookupInput2<D>((Class<D>) pmm.getActualType()); // Create a lookup thing for this one
		String hint = MetaUtils.findHintText(spm);
		if(null != hint)
			control.setHint(hint);
		control.setPopupSearchImmediately(spm.isPopupSearchImmediately());
		control.setPopupInitiallyCollapsed(spm.isPopupInitiallyCollapsed());
		return new FactoryPair<>(new ObjectLookupQueryBuilder<>(pmm.getName()), control);
	}
}
