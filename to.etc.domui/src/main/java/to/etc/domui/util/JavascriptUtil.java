package to.etc.domui.util;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.javascript.*;

final public class JavascriptUtil {
	private JavascriptUtil() {}

	/**
	 * Render a disable selection command. This disables selecting content with the mouse for copy/paste. It
	 * also disables the silly CTRL + LEFT_MOUSECLICK popup from IE.
	 *
	 * @param nb
	 * @return
	 */
	static public String	disableSelection(NodeBase nb) {
		return "WebUI.disableSelect('" + nb.getActualID() + "');";

		//return "$('#" + nb.getActualID() + "').disableSelection();";
	}

	/**
	 * Re-enable selection.
	 * @param nb
	 * @return
	 */
	static public String enableSelection(NodeBase nb) {
		return "WebUI.enableSelect('" + nb.getActualID() + "');";
		//return "$('#" + nb.getActualID() + "').enableSelection();";
	}

	static public void setThreePanelHeight(@Nonnull JavascriptStmt statement, @Nullable NodeBase top, @Nonnull NodeBase middle, @Nullable NodeBase bottom) throws Exception {
		JsMethod m = statement.method("WebUI.setThreePanelHeight");
		m.arg(top == null ? Integer.valueOf(0) : top.getActualID());
		m.arg(middle.getActualID());
		m.arg(bottom == null ? Integer.valueOf(0) : bottom.getActualID());
		m.end();
	}


}
