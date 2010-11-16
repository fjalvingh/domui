package to.etc.domui.component.lookup;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

/**
 * Represents factory for enum values or boolean values lookup. For lookup condition uses combo box automaticaly populated with localized values of enum constants or boolean values.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 1 Aug 2009
 */
final class LookupFactoryEnumAndBool implements ILookupControlFactory {
	@Override
	public <X extends IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	@Override
	public <X extends IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		IInputNode< ? > ctlnode = control;
		if(ctlnode == null) {
			PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);

			// Create a domainvalued combobox by default.
			Object[] vals = pmm.getDomainValues();
			ClassMetaModel ecmm = null;
			List<ValueLabelPair<Object>> vl = new ArrayList<ValueLabelPair<Object>>();
			for(Object o : vals) {
				String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
				if(label == null) {
					if(ecmm == null)
						ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
					label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
					if(label == null)
						label = o == null ? "" : o.toString();
				}
				vl.add(new ValueLabelPair<Object>(o, label));
			}

			final ComboFixed< ? > c = new ComboFixed<Object>(vl);
			String s = pmm.getDefaultHint();
			if(s != null) {
				c.setTitle(s);
			}
			String hint = MetaUtils.findHintText(spm);
			if(hint != null)
				c.setTitle(hint);
			ctlnode = c;
		}

		return new EqLookupControlImpl(spm.getPropertyName(), ctlnode);
	}
}
