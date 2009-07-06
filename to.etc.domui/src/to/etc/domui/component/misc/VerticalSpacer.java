package to.etc.domui.component.misc;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

public class VerticalSpacer extends Div {
	public VerticalSpacer(int height) {
		setHeight(height + "px");
		setText("\u00a0");
		setOverflow(Overflow.HIDDEN);
	}
}
