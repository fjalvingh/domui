package to.etc.domui.logic.events;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

final public class LogiEventPropertyChange<T> extends LogiEventBase {
	@Nonnull
	final private PropertyMetaModel<T> m_pmm;

	@Nullable
	final private T m_oldvalue, m_newvalue;

	public LogiEventPropertyChange(@Nonnull String path, @Nonnull PropertyMetaModel<T> pmm, @Nullable T oldvalue, @Nullable T newvalue) {
		super(path);
		m_pmm = pmm;
		m_oldvalue = oldvalue;
		m_newvalue = newvalue;
	}

	@Nonnull
	public PropertyMetaModel<T> getPmm() {
		return m_pmm;
	}

	@Nullable
	public T getOldvalue() {
		return m_oldvalue;
	}

	@Nullable
	public T getNewvalue() {
		return m_newvalue;
	}

	@Override
	void dump(@Nonnull Appendable a) throws Exception {
		a.append(getPath()).append(" [property ").append(m_pmm.toString()).append(" changed from ").append(toString(m_oldvalue)).append(" to ").append(toString(m_newvalue)).append("\n");
	}
}
