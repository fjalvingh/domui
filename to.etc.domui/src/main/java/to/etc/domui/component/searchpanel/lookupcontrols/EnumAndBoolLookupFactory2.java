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

import to.etc.domui.component.input.ValueLabelPair;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents factory for enum values or boolean values lookup. For lookup condition uses combo box automaticaly populated with localized values of enum constants or boolean values.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 1 Aug 2009
 */
final class EnumAndBoolLookupFactory2<T> implements ILookupFactory<T> {
	@Nonnull @Override public FactoryPair<T> createControl(@Nonnull SearchPropertyMetaModel spm) {
		PropertyMetaModel<T> pmm = (PropertyMetaModel<T>) spm.getProperty();

		// Create a domainvalued combobox by default.
		Object[] vals = pmm.getDomainValues();
		if(null == vals)
			throw new IllegalStateException(pmm + ": no domainValues");
		ClassMetaModel ecmm = null;
		List<ValueLabelPair<T>> vl = new ArrayList<>();
		for(Object o : vals) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o);	// Label known to property?
			if(label == null) {
				if(ecmm == null)
					ecmm = MetaManager.findClassMeta(pmm.getActualType()); 		// Try to get the property's type.
				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
				if(label == null)
					label = o == null ? "" : o.toString();
			}
			vl.add(new ValueLabelPair<>((T) o, label));
		}

		final ComboFixed2<T> c = new ComboFixed2<>(vl);
		String s = pmm.getDefaultHint();
		if(s != null) {
			c.setTitle(s);
		}
		String hint = MetaUtils.findHintText(spm);
		if(hint != null)
			c.setTitle(hint);
		return new FactoryPair<>(new ObjectLookupQueryBuilder<T>(pmm.getName()), c);
	}
}
