package to.etc.domui.dom.html;

public interface IHtmlInput {
	boolean isDisabled();

	void setDisabled(boolean yes);

	/**
	 * See {@link Input#isPasswordManagerAllowed()}: when T browser password managers may
	 * offer their inline fill/save menu on this control. Defaults to F on all implementations,
	 * so the managers are told to keep away unless this really is a credential field.
	 */
	boolean isPasswordManagerAllowed();

	/**
	 * See {@link #isPasswordManagerAllowed()}.
	 */
	void setPasswordManagerAllowed(boolean passwordManagerAllowed);
}
