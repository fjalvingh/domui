package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

public class LookupControlFactoryEnumAndBool implements LookupControlFactory {

	public int accepts(PropertyMetaModel pmm) {
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	public ILookupControlInstance createControl(SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {

		// Create a domainvalued combobox by default.
		Object[] vals = pmm.getDomainValues();
		ClassMetaModel ecmm = null;
		List<ComboFixed.Pair<Object>> vl = new ArrayList<ComboFixed.Pair<Object>>();
		for(Object o : vals) {
			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
			if(label == null) {
				if(ecmm == null)
					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
				if(label == null)
					label = o == null ? "" : o.toString();
			}
			vl.add(new ComboFixed.Pair<Object>(o, label));
		}

		final ComboFixed< ? > c = new ComboFixed<Object>(vl);
		if(pmm.isRequired()) {
			c.setMandatory(true);
		}
		String s = pmm.getDefaultHint();
		if(s != null) {
			c.setTitle(s);
		}
		return new AbstractLookupControlImpl(c) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object value = c.getValue();
				if(value != null) {
					crit.eq(pmm.getName(), value);
					return true;
				}
				return true; // Okay but no data
			}
		};
	}
}
