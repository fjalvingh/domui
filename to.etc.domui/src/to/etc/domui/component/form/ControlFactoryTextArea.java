package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class ControlFactoryTextArea implements ControlFactory {
	/**
	 * Accept if the componentHint says textarea.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(PropertyMetaModel pmm, boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(TextArea.class))
			return -1;
		if(pmm.getComponentTypeHint() != null) {
			if(pmm.getComponentTypeHint().toLowerCase().contains(MetaUtils.TEXT_AREA))
				return 10;
		}
		return 0;
	}

	@Override
	public ControlFactoryResult createControl(IReadOnlyModel< ? > model, PropertyMetaModel pmm, boolean editable, Class< ? > controlClass, Object context) {
		TextArea ta = new TextArea();
		if(!editable)
			ta.setReadOnly(true);
		String hint = pmm.getComponentTypeHint().toLowerCase();
		ta.setCols(MetaUtils.parseIntParam(hint, MetaUtils.COL, 80));
		ta.setRows(MetaUtils.parseIntParam(hint, MetaUtils.ROW, 4));
		if(pmm.isRequired())
			ta.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			ta.setTitle(s);
		return new ControlFactoryResult(ta, model, pmm);
	}
}
