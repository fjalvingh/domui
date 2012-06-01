package to.etc.domui.util;

import to.etc.domui.dom.html.*;

/**
 * Renders {@link DropMode#DIV} drag and drop layout decoration.
 * It relays on DivModeDragAndDropPlugin.js and DivModeDragAndDropPlugin.css resources.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Sep 2011
 */
public class DivModeDragAndDropPlugin implements IDragNdropPlugin {

	@Override
	public DropMode getMode() {
		return DropMode.DIV;
	}

	@Override
	public void renderDraggable(NodeBase base, IDragHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drgbl");
		} else{
			IDragArea a = dh.getDragArea();
			if(a == null)
				throw new IllegalStateException("Drag area should not be null");
			base.appendJavascript("DDD.makeDraggableById('" + a.getActualID() + "');");
			base.setSpecialAttribute("uitype", dh.getTypeName(base));
		}
	}

	@Override
	public void renderDroppable(NodeBase base, IDropHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drpbl");
		} else {
			//do nothing special...
		}
	}

}
