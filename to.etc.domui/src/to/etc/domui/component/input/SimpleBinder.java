package to.etc.domui.component.input;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * EXPERIMENTAL - DO NOT USE.
 * This is a simple binder implementation for base IInputNode<T> implementing controls. It handles all
 * binding chores.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2009
 */
public class SimpleBinder implements IBinder {
	private IInputNode< ? > m_control;

	/** If this contains whatever property-related binding this contains the property's meta model, needed to use it's value accessor. */
	private PropertyMetaModel m_propertyModel;

	/** If this is bound to some model this contains the model, */
	private IReadOnlyModel< ? > m_model;

	/** If this is bound to an object instance directly it contains the instance */
	private Object m_instance;

	/** If this thing is bound to some event listener... */
	private IBindingListener< ? > m_listener;

	public SimpleBinder(IInputNode< ? > control) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		m_control = control;
	}

	/**
	 * Returns T if this contains an actual binding. We are bound if property is set OR a listener is set.
	 * @see to.etc.domui.component.input.IBinder#isBound()
	 */
	public boolean isBound() {
		return m_propertyModel != null || m_listener != null;
	}

	/**
	 * Bind to a property of the object returned by this model.
	 * @see to.etc.domui.component.input.IBinder#to(java.lang.Class, to.etc.domui.util.IReadOnlyModel, java.lang.String)
	 */
	public <T> void to(Class<T> theClass, IReadOnlyModel<T> model, String property) {
		m_listener = null;
		m_propertyModel = MetaManager.getPropertyMeta(theClass, property);
		m_model = model;
		m_instance = null;
	}

	/**
	 *
	 * @see to.etc.domui.component.input.IBinder#to(to.etc.domui.component.input.IBindingListener)
	 */
	public void to(IBindingListener< ? > listener) {
		m_propertyModel = null;
		m_instance = null;
		m_model = null;
		m_listener = listener;
	}

	/**
	 * Bind to a property of the instance specified.
	 *
	 * @see to.etc.domui.component.input.IBinder#to(java.lang.Object, java.lang.String)
	 */
	public void to(Object instance, String property) {
		if(instance == null)
			throw new IllegalArgumentException("The instance in a component bind request CANNOT be null!");
		m_listener = null;
		m_model = null;
		m_propertyModel = MetaManager.getPropertyMeta(instance.getClass(), property);
		m_instance = instance;
	}
}
