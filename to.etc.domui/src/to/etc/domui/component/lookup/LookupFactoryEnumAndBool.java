package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 * Represents factory for enum values or boolean values lookup. For lookup condition uses combo box automaticaly populated with localized values of enum constants or boolean values.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 1 Aug 2009
 */
public class LookupFactoryEnumAndBool implements ILookupControlFactory {
	public int accepts(SearchPropertyMetaModel spm) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	public ILookupControlInstance createControl(final SearchPropertyMetaModel spm) {
		PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);

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
		String hint = MetaUtils.findHintText(spm);
		if(hint != null)
			c.setTitle(hint);

		return new AbstractLookupControlImpl(c) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object value = c.getValue();
				if(value != null) {
					crit.eq(spm.getPropertyName(), value);
					return true;
				}
				return true; // Okay but no data
			}
		};
	}
}
