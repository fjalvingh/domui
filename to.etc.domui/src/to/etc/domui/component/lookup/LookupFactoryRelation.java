package to.etc.domui.component.lookup;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

final class LookupFactoryRelation implements ILookupControlFactory {
	public <X extends to.etc.domui.dom.html.IInputNode< ? >> int accepts(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		if(pmm.getRelationType() ==  PropertyRelationType.UP) {		// Accept only relations.
			return 4;
		}
		return -1;
	}

	public <X extends to.etc.domui.dom.html.IInputNode< ? >> ILookupControlInstance createControl(final SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
		IInputNode< ? > input = control;
		if(input == null) {
			final LookupInput<Object> l = new LookupInput<Object>((Class<Object>) pmm.getActualType()); // Create a lookup thing for this one
			String hint = MetaUtils.findHintText(spm);
			l.setHint(hint);
			input = l;
		}
		return new EqLookupControlImpl(spm.getPropertyName(), input);
	}
}
