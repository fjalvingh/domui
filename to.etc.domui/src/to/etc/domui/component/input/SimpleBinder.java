package to.etc.domui.component.input;

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

	public SimpleBinder(IInputNode< ? > control) {
		if(control == null)
			throw new IllegalArgumentException("The control cannot be null.");
		m_control = control;
	}

	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> void to(Class<T> theClass, IReadOnlyModel<T> model, String property) {
	// TODO Auto-generated method stub

	}

	public void to(IBindingListener< ? > listener) {

	}

	public void to(Object instance, String property) {
	// TODO Auto-generated method stub

	}
}
