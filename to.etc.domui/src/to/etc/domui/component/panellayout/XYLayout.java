package to.etc.domui.component.panellayout;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class XYLayout implements ILayoutManager {
	@Override
	public void place(@Nonnull LayoutPanelBase target, @Nonnull NodeBase node, @Nonnull Object layoutOptions) {
		IntPoint pt = (IntPoint) layoutOptions;
		if(!node.hasParent() || node.getParent() != target)
			target.add(node);
		node.setPosition(PositionType.ABSOLUTE);
		node.setTop(pt.y());
		node.setLeft(pt.x());
	}
}
