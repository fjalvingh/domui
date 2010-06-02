package to.etc.domui.component.form;

import to.etc.domui.util.*;

public class InstanceReadOnlyModel<T> implements IReadOnlyModel<T> {
	private T m_instance;

	public InstanceReadOnlyModel(T instance) {
		m_instance = instance;
	}

	@Override
	public T getValue() throws Exception {
		return m_instance;
	}
}
