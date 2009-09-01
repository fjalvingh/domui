package to.etc.domui.util;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

public class DefaultControlLabelFactory implements IControlLabelFactory {
	public Label createControlLabel(NodeBase control, String text, boolean editable, boolean mandatory, PropertyMetaModel pmm) {
		if(text == null)
			return null;
		if(mandatory)
			text = "* " + text;
		if(pmm != null && editable && NumericPresentation.isMonetary(pmm.getNumericPresentation()))
			text = text + " \u20ac";

		Label l = new Label(control, text);
		l.setCssClass("ui-f-lbl");
		return l;
	}
}
