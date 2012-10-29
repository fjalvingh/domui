package to.etc.domui.logic;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

public class LogiEventPropertyChange<T> extends LogiEventBase {
	private PropertyMetaModel<T> m_pmm;

	private T m_oldvalue, m_newvalue;

	public LogiEventPropertyChange(@Nonnull String path, PropertyMetaModel<T> pmm, T oldvalue, T newvalue) {
		super(path);
		m_pmm = pmm;
		m_oldvalue = oldvalue;
		m_newvalue = newvalue;
	}

	public PropertyMetaModel<T> getPmm() {
		return m_pmm;
	}

	public T getOldvalue() {
		return m_oldvalue;
	}

	public T getNewvalue() {
		return m_newvalue;
	}
}
