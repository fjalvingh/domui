package to.etc.domui.dom.html;

/**
 * A non-input control that usually only controls some action, like a button
 * or tab pane tab. They can only be enabled and disabled, and someone can
 * listen to changes (button presses) on the component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 6, 2009
 */
public interface IActionControl {
	/**
	 * Set the input to disabled mode.
	 * @param d
	 */
	void setDisabled(boolean d);

	/**
	 * Set the testID for external test software.
	 * @param testID
	 */
	void setTestID(String testID);
}
