package to.etc.domui.dom.errors;

public interface INodeErrorDelegate {
	/**
	 * This sets a message (an error, warning or info message) on this control. If the
	 * control already has an error then we check if the severity of the new error is
	 * higher than the severity of the existing one; only in that case will the error
	 * be removed. To clear the error message call clearMessage().
	 *
	 * @param mt
	 * @param code
	 * @param param
	 */
	public UIMessage	setMessage(MsgType mt, String code, Object... param);

	/**
	 * Remove this-component's "current" error message, if present.
	 */
	public void	clearMessage();

	/**
	 * @see to.etc.domui.dom.html.IInputBase#getMessage()
	 */
	public UIMessage getMessage();
}
