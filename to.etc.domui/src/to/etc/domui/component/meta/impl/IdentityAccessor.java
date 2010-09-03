package to.etc.domui.component.meta.impl;

import to.etc.domui.util.*;

/**
 * Accessor which returns the same object as it's input.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class IdentityAccessor<T> implements IValueAccessor<T> {
	@Override
	public T getValue(Object in) throws Exception {
		InstanceRefresher.refresh(in);
		return (T) in;
	}

	@Override
	public void setValue(Object target, T value) throws Exception {
		throw new IllegalStateException("Can't set the value for this object.");
	}
}
