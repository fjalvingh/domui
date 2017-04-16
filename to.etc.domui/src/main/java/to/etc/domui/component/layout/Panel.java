package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public class Panel extends Div {
	public Panel() {
		setCssClass("ui-spnl");
	}

	public Panel(@Nonnull String css) {
		setCssClass(css);
	}
}
