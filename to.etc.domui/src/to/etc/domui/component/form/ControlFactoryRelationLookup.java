package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * Accepts any UP (parent) relation and scores 3, preferring this above the combobox-based
 * lookup.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlFactoryRelationLookup implements ControlFactory {
	/**
	 * Accept any UP relation.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(LookupInput.class))
			return -1;

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return 0;
		if(Constants.COMPONENT_LOOKUP.equals(pmm.getComponentTypeHint()))
			return 10;
		return 3; // Prefer a lookup above a combo if unspecified
	}

	/**
	 * Create the lookup thingy.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#createControl(to.etc.domui.util.IReadOnlyModel, to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public Result createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass) {
		//-- We'll do a lookup thingy for sure.
		LookupInput<Object> li = new LookupInput<Object>((Class<Object>) pmm.getActualType());
		li.setReadOnly(!editable);
		if(pmm.getLookupFieldRenderer() != null)
			li.setContentRenderer((INodeContentRenderer<Object>) DomApplication.get().createInstance(pmm.getLookupFieldRenderer())); // Bloody stupid Java generic crap
		else {
			ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType()); // Get meta for type reached,
			if(cmm.getLookupFieldRenderer() != null)
				li.setContentRenderer((INodeContentRenderer<Object>) DomApplication.get().createInstance(cmm.getLookupFieldRenderer())); // Bloody stupid Java generic crap
		}
		if(pmm.isRequired())
			li.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			li.setTitle(s);
		return new Result(li, model, pmm);
	}
}

