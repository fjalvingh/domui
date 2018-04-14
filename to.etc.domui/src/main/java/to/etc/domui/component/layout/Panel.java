package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.Div;

public class Panel extends Div {
	public Panel() {
		setCssClass("ui-spnl");
	}

	public Panel(@NonNull String css) {
		setCssClass(css);
	}
}
