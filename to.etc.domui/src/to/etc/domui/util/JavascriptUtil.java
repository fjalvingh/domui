package to.etc.domui.util;

import to.etc.domui.dom.html.*;

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
		return "$('#" + nb.getActualID() + "').disableSelection();";
	}

	/**
	 * Re-enable selection.
	 * @param nb
	 * @return
	 */
	static public String enableSelection(NodeBase nb) {
		return "$('#" + nb.getActualID() + "').enableSelection();";
	}

//	static public void	setThreePanelHeight(@Nonnull )


}
