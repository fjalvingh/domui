package to.etc.domui.dom.html;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.trouble.*;

/**
 * Generic representation of a control, having input and some state.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2009
 */
public interface IInputNode<T> extends IHasChangeListener, IBindable, INodeErrorDelegate {
	/**
	 * Returns the current value of this input component. If the component contains no value
	 * this returns null. All text input components will return null when their value is the
	 * empty string! If the component, during conversion or validation of the input value,
	 * discovers that the input is invalid it will set itself in "error" mode, post an error
	 * message up the form, and throw a {@link ValidationException}. This means that this
	 * call either delivers correct input (as defined by it's converter and validators), null
	 * (when empty) or throws an exception. When a ValidationException occurs the framework
	 * mostly ignores it - it does not produce a stacktrace or error in the client. Instead
	 * the resulting error as posted by the error handling framework gets displayed on the
	 * form when the request completes.
	 * <p>To get the value of a component while ignoring exceptions call {@link #getValueSafe()}.</p>
	 * @return
	 */
	T getValue();

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
	 * Set a new value into this control. Setting a value to null means the control holds no value. This
	 * value is converted to a presentable form using any (implicitly) defined converters; it will not
	 * be validated though! This means that if you set an invalid value for a validator this will not
	 * be seen until the value is gotten from the control again.
	 * @param v
	 */
	void setValue(T v);

	boolean isReadOnly();

	void setReadOnly(boolean ro);

	boolean isDisabled();

	void setDisabled(boolean d);

	boolean isMandatory();

	void setMandatory(boolean ro);

	void setTestID(String testID);
}
