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

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

final class LookupFactoryDate implements ILookupControlFactory {
	@Override
	public <T, X extends IControl<T>> ILookupControlInstance createControl(@Nonnull final SearchPropertyMetaModel spm, final X control) {
		if(spm == null)
			throw new IllegalStateException("? SearchPropertyModel should not be null here.");

		//get temporal type from metadata and set withTime later to date inout components
		PropertyMetaModel< ? > pmm = (spm != null && spm.getPropertyPath() != null && spm.getPropertyPath().size() > 0) ? spm.getPropertyPath().get(spm.getPropertyPath().size() - 1) : null;

		/*
		 * jal 20120712 By default, do not search with time on date fields, unless the "usetime" hint is present.
		 */
		boolean withTime = false;
		if(pmm != null && pmm.getTemporal() == TemporalPresentationType.DATETIME) {
			String value = DomUtil.getHintValue(pmm.getComponentTypeHint(), "time");
			if(null != value)
				withTime = true;
		}

		final DateInput dateFrom = new DateInput();
		dateFrom.setWithTime(withTime);
		TextNode tn = new TextNode(Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_DATE_TILL) + " ");
		final DateInput dateTo = new DateInput();
		dateTo.setWithTime(withTime);

		String hint = MetaUtils.findHintText(spm);
		if(hint != null) {
			dateFrom.setTitle(hint);
			dateTo.setTitle(hint);
		}
		return new AbstractLookupControlImpl(dateFrom, tn, dateTo) {
			@Override
			public AppendCriteriaResult appendCriteria(QCriteria< ? > crit) throws Exception {
				if(spm == null)
					throw new IllegalStateException("? SearchPropertyModel should not be null here.");
				Date from, till;
				try {
					from = dateFrom.getValue();
					//in case of date only search truncate time
					if(from != null && !dateFrom.isWithTime()) {
						from = DateUtil.truncateDate(from);
					}
				} catch(Exception x) {
					return AppendCriteriaResult.INVALID;
				}
				try {
					till = dateTo.getValue();
					//in case of date only search add 1 day and truncate time, since date only search is inclusive for dateTo
					if(till != null && !dateTo.isWithTime()) {
						till = DateUtil.truncateDate(DateUtil.incrementDate(till, Calendar.DATE, 1));
					}

				} catch(Exception x) {
					return AppendCriteriaResult.INVALID;
				}
				if(from == null && till == null)
					return AppendCriteriaResult.EMPTY;
				if(from != null && till != null) {
					if(from.getTime() > till.getTime()) {
						//-- Swap vals
						dateFrom.setValue(till);
						dateTo.setValue(from);
						Date tmp = from;
						from = till;
						till = tmp;
					}

					//-- Between query
					crit.ge(spm.getPropertyName(), from);
					crit.lt(spm.getPropertyName(), till);
				} else if(from != null) {
					crit.ge(spm.getPropertyName(), from);
				} else if(till != null) {
					crit.lt(spm.getPropertyName(), till);
				} else
					throw new IllegalStateException("Logic error");
				return AppendCriteriaResult.VALID;
			}

			@Override
			public void clearInput() {
				dateFrom.setValue(null);
				dateTo.setValue(null);
			}
		};
	}

	@Override
	public <T, X extends IControl<T>> int accepts(@Nonnull SearchPropertyMetaModel spm, X control) {
		PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);
		if(Date.class.isAssignableFrom(pmm.getActualType()) && control == null)
			return 2;
		return 0;
	}
}