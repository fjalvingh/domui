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
				dt.setValue(null);
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