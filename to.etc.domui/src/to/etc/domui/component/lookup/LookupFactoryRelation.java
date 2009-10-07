package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.webapp.query.*;

public class LookupFactoryRelation implements ILookupControlFactory {
	public int accepts(SearchPropertyMetaModel spm) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		if(pmm.getRelationType() ==  PropertyRelationType.UP) {		// Accept only relations.
			return 4;
		}
		return -1;
	}

	public ILookupControlInstance createControl(final SearchPropertyMetaModel spm) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);

		final LookupInput<Object> l = new LookupInput<Object>((Class<Object>) pmm.getActualType()); // Create a lookup thing for this one
		return new AbstractLookupControlImpl(l) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object	value = l.getValue();
				if(value != null) {
					crit.eq(spm.getPropertyName(), value);
					return true;
				}
				return true;			// Okay but no data
			}
		};
	}
}
