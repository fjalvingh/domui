package to.etc.domui.component.input;

import javax.annotation.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * A thingy which handles binding a control to a model/property, data move event or
 * IReadOnlyModel/property.
 *
 * FIXME Question - should this <i>be</i> a IModelBinding or should this <i>have</i> a IModelBinding?
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public interface IBinder extends IModelBinding {
	/**
	 * Create a binding to the associated control and the specified object instance and the named property of that instance.
	 * @param instance
	 * @param property
	 */
	void to(@Nonnull Object instance, @Nonnull String property);

	/**
	 * Create a binding to the associated instance's property whose metadata is passed.
	 * @param instance
	 * @param pmm
	 */
	void to(@Nonnull Object instance, @Nonnull PropertyMetaModel pmm);

	/**
	 * Create a binding between the associated control, the specified model and the property specified.
	 * @param <T>
	 * @param theClass
	 * @param model
	 * @param property
	 */
	<T> void to(@Nonnull Class<T> theClass, @Nonnull IReadOnlyModel<T> model, @Nonnull String property);

	/**
	 * Create a binding between the specified model and the property whose metadata is passed in.
	 * @param <T>
	 * @param model		The model to obtain an instance from
	 * @param pmm		The propertymeta for a property on that instance.
	 */
	<T> void to(@Nonnull IReadOnlyModel<T> model, @Nonnull PropertyMetaModel pmm);

	/**
	 * Bind the control to a listener.
	 * @param listener
	 */
	void to(@Nonnull IBindingListener< ? > listener);

	/**
	 * If this object is actually bound to something return true.
	 *
	 * @return
	 */
	boolean isBound();

}
