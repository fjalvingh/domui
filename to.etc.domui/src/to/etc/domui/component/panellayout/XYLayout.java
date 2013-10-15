package to.etc.domui.component.panellayout;

import java.awt.*;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

public class XYLayout implements ILayoutManager {
	@Override
	public void place(@Nonnull LayoutPanelBase target, @Nonnull NodeBase node, @Nonnull Object layoutOptions) {
		Point pt = (Point) layoutOptions;
		target.add(node);
		node.setPosition(PositionType.ABSOLUTE);
		node.setTop(pt.y);
		node.setLeft(pt.x);
	}
}
