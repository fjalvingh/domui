package to.etc.iocular.def;

import java.lang.reflect.*;

public class PropertyInjector {
	private final ComponentRef m_ref;

	private final Method m_setter;

	public PropertyInjector(final ComponentRef ref, final Method setter) {
		m_ref = ref;
		m_setter = setter;
	}

	public ComponentRef getRef() {
		return m_ref;
	}

	public Method getSetter() {
		return m_setter;
	}
}
