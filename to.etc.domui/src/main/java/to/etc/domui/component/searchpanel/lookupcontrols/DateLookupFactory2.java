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

import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;

final class DateLookupFactory2 implements ILookupFactory<DatePeriod> {
	@Override public FactoryPair<DatePeriod> createControl(@Nonnull SearchPropertyMetaModel spm) {
		//get temporal type from metadata and set withTime later to date inout components
		PropertyMetaModel<?> pmm = spm.getProperty();

		/*
		 * jal 20120712 By default, do not search with time on date fields, unless the "usetime" hint is present.
		 */
		boolean withTime = false;
		if(pmm != null && pmm.getTemporal() == TemporalPresentationType.DATETIME) {
			String value = DomUtil.getHintValue(pmm.getComponentTypeHint(), "time");
			if(null != value)
				withTime = true;
		}
		DateLookupControl control = new DateLookupControl();
		control.setWithTime(withTime);
		return new FactoryPair<>(new DateLookupQueryBuilder(pmm.getName()), control);
	}
}
