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
	public <X extends to.etc.domui.dom.html.IInputNode< ? >> ILookupControlInstance createControl(@Nonnull final SearchPropertyMetaModel spm, final X control) {
		if(spm == null)
			throw new IllegalStateException("? SearchPropertyModel should not be null here.");

		//get temporal type from metadata and set withTime later to date inout components
		PropertyMetaModel pmm = (spm != null && spm.getPropertyPath() != null && spm.getPropertyPath().size() > 0) ? spm.getPropertyPath().get(spm.getPropertyPath().size() - 1) : null;
		boolean withTime = (pmm != null && pmm.getTemporal() == TemporalPresentationType.DATETIME);

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
			// FIXME For some reason Eclipse does not "see" the null check @ the start of the method..
			@SuppressWarnings("null")
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Date from, till;
				try {
					from = dateFrom.getValue();
					//in case of date only search truncate time
					if(from != null && !dateFrom.isWithTime()) {
						from = DateUtil.truncateDate(from);
					}
				} catch(Exception x) {
					return false;
				}
				try {
					till = dateTo.getValue();
					//in case of date only search add 1 day and truncate time, since date only search is inclusive for dateTo
					if(till != null && !dateTo.isWithTime()) {
						till = DateUtil.truncateDate(DateUtil.incrementDate(till, Calendar.DATE, 1));
					}

				} catch(Exception x) {
					return false;
				}
				if(from == null && till == null)
					return true;
				if(from != null && till != null) {
					if(from.getTime() > till.getTime()) {
						//-- Swap vals
						dateFrom.setValue(till);
						dateTo.setValue(from);
						from = till;
						till = dateTo.getValue();
					}

					//-- Between query
					crit.ge(spm.getPropertyName(), from);
					crit.lt(spm.getPropertyName(), till);
				} else if(from != null) {
					crit.ge(spm.getPropertyName(), from);
				} else {
					crit.lt(spm.getPropertyName(), till);
				}
				return true;
			}

			@Override
			public void clearInput() {
				dateFrom.setValue(null);
				dateTo.setValue(null);
			}
		};
	}

	public <X extends to.etc.domui.dom.html.IInputNode< ? >> int accepts(SearchPropertyMetaModel spm, X control) {
		PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		if(Date.class.isAssignableFrom(pmm.getActualType()) && control == null)
			return 2;
		return 0;
	}
}