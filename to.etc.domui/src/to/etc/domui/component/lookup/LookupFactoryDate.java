package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

final class LookupFactoryDate implements ILookupControlFactory {
	public ILookupControlInstance createControl(final SearchPropertyMetaModel spm) {
		final DateInput dateFrom = new DateInput();
		TextNode tn = new TextNode(NlsContext.getGlobalMessage(Msgs.UI_LOOKUP_DATE_TILL));
		final DateInput dateTo = new DateInput();

		String hint = MetaUtils.findHintText(spm);
		if(hint != null) {
			dateFrom.setTitle(hint);
			dateTo.setTitle(hint);
		}
		return new AbstractLookupControlImpl(dateFrom, tn, dateTo) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Date from, till;
				try {
					from = dateFrom.getValue();
				} catch(Exception x) {
					return false;
				}
				try {
					till = dateTo.getValue();
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
					crit.between(spm.getPropertyName(), from, till);
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

	public int accepts(SearchPropertyMetaModel spm) {
		PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		if(Date.class.isAssignableFrom(pmm.getActualType()))
			return 2;
		return 0;
	}
}