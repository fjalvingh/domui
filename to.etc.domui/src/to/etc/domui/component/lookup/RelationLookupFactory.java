package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.webapp.query.*;

public class RelationLookupFactory implements LookupControlFactory {
	public int accepts(PropertyMetaModel pmm) {
		if(pmm.getRelationType() ==  PropertyRelationType.UP) {		// Accept only relations.
			return 4;
		}
		return -1;
	}

	public LookupFieldQueryBuilderThingy createControl(final SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
		final LookupInput<Object>		l = new LookupInput<Object>((Class) pmm.getActualType());			// Create a lookup thing for this one
		return new DefaultLookupThingy() {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object	value = l.getValue();
				if(value != null) {
					crit.eq(pmm.getName(), value);
					return true;
				}
				return false;
			}
		};
	}
}
