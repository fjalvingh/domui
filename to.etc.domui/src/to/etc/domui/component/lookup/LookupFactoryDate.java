package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

final class LookupFactoryDate implements LookupControlFactory {
	public ILookupControlInstance createControl(SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
		final DateInput df = new DateInput();
		TextNode tn = new TextNode(NlsContext.getGlobalMessage(Msgs.UI_LOOKUP_DATE_TILL));
		final DateInput dt = new DateInput();
		return new AbstractLookupControlImpl(df, tn, dt) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Date from, till;
				try {
					from = df.getValue();
				} catch(Exception x) {
					return false;
				}
				try {
					till = dt.getValue();
				} catch(Exception x) {
					return false;
				}
				if(from == null && till == null)
					return true;
				if(from != null && till != null) {
					if(from.getTime() > till.getTime()) {
						//-- Swap vals
						df.setValue(till);
						dt.setValue(from);
						from = till;
						till = dt.getValue();
					}

					//-- Between query
					crit.between(pmm.getName(), from, till);
				} else if(from != null) {
					crit.ge(pmm.getName(), from);
				} else {
					crit.lt(pmm.getName(), till);
				}
				return true;
			}
		};
	}

	public int accepts(PropertyMetaModel pmm) {
		if(Date.class.isAssignableFrom(pmm.getActualType()))
			return 2;
		return 0;
	}
}