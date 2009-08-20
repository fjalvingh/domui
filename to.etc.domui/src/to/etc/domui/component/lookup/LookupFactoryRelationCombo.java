package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

public class LookupFactoryRelationCombo implements LookupControlFactory {
	public int accepts(PropertyMetaModel pmm) {
		if(pmm.getRelationType() != PropertyRelationType.UP)
			return -1;
		if(Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
			return 10;
		return 2;
	}

	@SuppressWarnings("unchecked")
	public ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final PropertyMetaModel pmm) {
		//-- We need to add a ComboBox. Do we have a combobox dataset provider?
		Class< ? extends IComboDataSet< ? >> set = pmm.getComboDataSet();
		if(set == null) {
			set = pmm.getClassModel().getComboDataSet();
			if(set == null)
				throw new IllegalStateException("Missing Combo dataset provider for property " + pmm);
		}

		INodeContentRenderer< ? > r = MetaManager.createDefaultComboRenderer(pmm, null);
		final ComboLookup< ? > co = new ComboLookup(set, r);
		if(pmm.isRequired())
			co.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			co.setTitle(s);

		return new AbstractLookupControlImpl(co) {
			@Override
			public boolean appendCriteria(QCriteria< ? > crit) throws Exception {
				Object value = co.getValue();
				if(value != null) {
					crit.eq(pmm.getName(), value);
					return true;
				}
				return true; // Okay but no data
			}
		};
	}
}
