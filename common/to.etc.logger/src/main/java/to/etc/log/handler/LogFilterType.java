package to.etc.log.handler;

public enum LogFilterType {

	MDC(true), SESSION(false);

	private final boolean	m_persistable;

	LogFilterType(boolean persistable) {
		m_persistable = persistable;
	}

	boolean isPersistable() {
		return m_persistable;
	}
}
