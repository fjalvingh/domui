package to.etc.domui.util;

import to.etc.domui.dom.html.*;

public final class UIDragDropUtil {
	private UIDragDropUtil() {}

	/**
	 * Expose all draggable thingies on a node.
	 * @param base
	 * @param dh
	 */
	static public void exposeDraggable(NodeBase base, IDragHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drgbl");
			base.setOnMouseDownJS(null);
		} else {
			base.addCssClass("ui-drgbl");
			base.setOnMouseDownJS("WebUI.dragMouseDown(this, event)");
			base.setSpecialAttribute("uitype", dh.getTypeName(base));
		}
	}

	static public void exposeDroppable(NodeBase base, IDropHandler dh) {
		if(dh == null) {
			base.removeCssClass("ui-drpbl");
		} else {
			base.addCssClass("ui-drpbl");
			StringBuilder sb = new StringBuilder();
			if(dh.getAcceptableTypes() == null)
				throw new IllegalStateException("The IDropHandler for node " + base + " (" + dh + ") returns a null list of acceptable types");
			for(String s : dh.getAcceptableTypes()) {
				if(sb.length() != 0)
					sb.append(',');
				sb.append(s);
			}
			base.setSpecialAttribute("uitypes", sb.toString());
		}
	}
}
