package to.etc.domui.component.meta.impl;

import to.etc.domui.util.*;

public class SubAccessor<B, V> implements IValueAccessor<V> {
	/** This accessor provides us with the ROOT value to use, */
	private IValueAccessor<B>		m_rootAccessor;

	/** This accessor then uses the root value and transforms it in the property's value on that object. */
	private IValueAccessor<V>		m_valueAccessor;

	public SubAccessor(IValueAccessor<B> rootAccessor, IValueAccessor<V> valueAccessor) {
		m_rootAccessor = rootAccessor;
		m_valueAccessor = valueAccessor;
	}
	public V getValue(Object in) throws Exception {
		Object	root	= m_rootAccessor.getValue(in);
		if(root == null)
			return null;
		InstanceRefresher.refresh(in);
		return m_valueAccessor.getValue(root);
	}
	public void setValue(Object target, V value) throws Exception {
		Object root	= m_rootAccessor.getValue(target);
		if(root == null)
			throw new IllegalStateException("The value is null: cannot reach a relational object.");
		m_valueAccessor.setValue(root, value);
	}
}
