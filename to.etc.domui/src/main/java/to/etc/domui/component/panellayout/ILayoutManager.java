package to.etc.domui.component.panellayout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.NodeBase;

public interface ILayoutManager {
	void place(@NonNull LayoutPanelBase layoutPanelBase, @NonNull NodeBase node, @NonNull Object layoutOptions);

}
