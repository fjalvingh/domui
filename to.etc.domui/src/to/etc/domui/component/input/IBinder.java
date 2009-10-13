package to.etc.domui.component.input;

import to.etc.domui.util.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * A thingy which handles binding a control to a model/property, data move event or
 * IReadOnlyModel/property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBinder {
	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * Create a binding to the associated control and the specified object instance and the named property of that instance.
	 * @param instance
	 * @param property
	 */
	void to(Object instance, String property);

	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * Create a binding between the associated control, the specified model and the property specified.
	 * @param <T>
	 * @param theClass
	 * @param model
	 * @param property
	 */
	<T> void to(Class<T> theClass, IReadOnlyModel<T> model, String property);

	/**
	 * Bind the control to a listener.
	 * EXPERIMENTAL - DO NOT USE.
	 * @param listener
	 */
	void to(IBindingListener< ? > listener);

	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * If this object is actually bound to something return true.
	 *
	 * @return
	 */
	boolean isBound();

}
