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

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

/**
 * Represents factory for enum values or boolean values lookup. For lookup condition uses combo box automaticaly populated with localized values of enum constants or boolean values.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 1 Aug 2009
 */
final class LookupFactoryEnumAndBool implements ILookupControlFactory {
	@Override
	public <X extends IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	@Override
	public <X extends IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		IInputNode< ? > ctlnode = control;
		if(ctlnode == null) {
			PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);

			// Create a domainvalued combobox by default.
			Object[] vals = pmm.getDomainValues();
			if(null == vals)
				throw new IllegalStateException(pmm + ": no domainValues");
			ClassMetaModel ecmm = null;
			List<ValueLabelPair<Object>> vl = new ArrayList<ValueLabelPair<Object>>();
			for(Object o : vals) {
				String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
				if(label == null) {
					if(ecmm == null)
						ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
					label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
					if(label == null)
						label = o == null ? "" : o.toString();
				}
				vl.add(new ValueLabelPair<Object>(o, label));
			}

			final ComboFixed< ? > c = new ComboFixed<Object>(vl);
			String s = pmm.getDefaultHint();
			if(s != null) {
				c.setTitle(s);
			}
			String hint = MetaUtils.findHintText(spm);
			if(hint != null)
				c.setTitle(hint);
			ctlnode = c;
		}

		return new EqLookupControlImpl(spm.getPropertyName(), ctlnode);
	}
}
