package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Accepts both enum and bools and shows a combobox with the possible choices.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryEnumAndBool implements ControlFactory {
	/**
	 * Accept boolean, Boolean and Enum.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboFixed.class)) // This one only creates ComboFixed thingies
			return -1;
		Class< ? > iclz = pmm.getActualType();
		return iclz == Boolean.class || iclz == Boolean.TYPE || Enum.class.isAssignableFrom(iclz) ? 2 : 0;
	}

	/**
	 * Create and init a ComboFixed combobox.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		ComboFixed< ? > c = DomApplication.get().getControlBuilder().createComboFor(pmm, editable);

		//		// Create a domainvalued combobox by default.
		//		Object[] vals = pmm.getDomainValues();
		//		ClassMetaModel ecmm = null;
		//		List<ComboFixed.Pair<Object>> vl = new ArrayList<ComboFixed.Pair<Object>>();
		//		for(Object o : vals) {
		//			String label = pmm.getDomainValueLabel(NlsContext.getLocale(), o); // Label known to property?
		//			if(label == null) {
		//				if(ecmm == null)
		//					ecmm = MetaManager.findClassMeta(pmm.getActualType()); // Try to get the property's type.
		//				label = ecmm.getDomainLabel(NlsContext.getLocale(), o);
		//				if(label == null)
		//					label = o == null ? "" : o.toString();
		//			}
		//			vl.add(new ComboFixed.Pair<Object>(o, label));
		//		}
		//
		//		ComboFixed< ? > c = new ComboFixed<Object>(vl);
		//		if(pmm.isRequired())
		//			c.setMandatory(true);
		//		if(!editable || pmm.getReadOnly() == YesNoType.YES)
		//			c.setDisabled(true);
		//		String s = pmm.getDefaultHint();
		//		if(s != null)
		//			c.setTitle(s);
		return new Result(c, model, pmm);
	}
}
