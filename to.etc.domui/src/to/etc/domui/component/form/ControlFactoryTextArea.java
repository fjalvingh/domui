package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class ControlFactoryTextArea implements ControlFactory {
	/**
	 * Accept if the componentHint says textarea.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	public int accepts(PropertyMetaModel pmm, boolean editable) {
		if(pmm.getComponentTypeHint() != null) {
			if(pmm.getComponentTypeHint().toLowerCase().contains("textarea"))
				return 10;
		}
		return 0;
	}

	public Result createControl(IReadOnlyModel< ? > model, PropertyMetaModel pmm, boolean editable) {
		TextArea ta = new TextArea();
		if(!editable)
			ta.setReadOnly(true);
		ta.setCols(80);
		ta.setRows(4);
		if(pmm.isRequired())
			ta.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			ta.setLiteralTitle(s);
		return new Result(ta, model, pmm);
	}
}
