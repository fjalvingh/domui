package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

final class LookupFactoryRelationCombo implements ILookupControlFactory {
	@Override
	public <X extends IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return -1;
		if(control == null && Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
			return 10;
		return 2;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <X extends IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		IInputNode< ? > input = control;
		if(input == null) {
			final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);

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
			String hint = MetaUtils.findHintText(spm);
			if(hint != null)
				co.setTitle(hint);
			input = co;
		}
		return new EqLookupControlImpl(spm.getPropertyName(), input);
	}
}
