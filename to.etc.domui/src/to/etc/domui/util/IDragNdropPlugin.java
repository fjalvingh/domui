package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public interface IDragNdropPlugin {

	DropMode getMode();

	void renderDraggable(NodeBase base, IDragHandler dh);

	void renderDroppable(NodeBase base, IDropHandler dh);

}
