package to.etc.domui.dom.html;

import to.etc.domui.dom.errors.*;

/**
 * Something to control a control: some input or control comnponent in a generic way. This
 * interface is allowed to be implemented on non-nodebase object.
 *
 * FIXME Should this also be a IBindable control?
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 6, 2009
 */
public interface IControl<T> extends IDisplayControl<T>, IActionControl, IHasChangeListener, INodeErrorDelegate {
	/**
	 * Like {@link #getValue()} this returns the value of the component, but this returns null
	 * if the value was invalid. For this method NULL either means the value was invalid OR
	 * it's content was empty. To distinguish between the two call {@link #hasError()} or use
	 * {@link #getValue()} instead of this call.
	 * @return
	 */
	T getValueSafe();

	/**
	 * Returns T if this control is currently in error state, meaning it's input is in some way
	 * invalid. This call internally calls getValue() to ensure it's error state is valid for
	 * the current data held.
	 * @return
	 */
	boolean hasError();

	/**
	 * Sets the input to readonly-mode. Components that do not implement readonly mode (comboboxes)
	 * will usually set themselves to disabled which works much the same.
	 * @param ro
	 */
	void setReadOnly(boolean ro);

	/**
	 * Make the control mandatory.
	 * @param ro
	 */
	void setMandatory(boolean ro);

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @param errorLocation
	 */
	void setErrorLocation(String errorLocation);

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @return
	 */
	String getErrorLocation();
}
