package to.etc.domui.component.panellayout;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.css.PositionType;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IntPoint;

public class XYLayout implements ILayoutManager {
	@Override
	public void place(@NonNull LayoutPanelBase target, @NonNull NodeBase node, @NonNull Object layoutOptions) {
		IntPoint pt = (IntPoint) layoutOptions;
		if(!node.hasParent() || node.getParent() != target)
			target.add(node);
		node.setPosition(PositionType.ABSOLUTE);
		node.setTop(pt.y());
		node.setLeft(pt.x());
	}
}
