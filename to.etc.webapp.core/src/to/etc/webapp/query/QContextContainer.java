package to.etc.webapp.query;

import javax.annotation.*;

public class QContextContainer {
	private QDataContext m_context;

	private QDataContextFactory m_factory;

	public QDataContext internalGetSharedContext() {
		return m_context;
	}

	public void internalSetSharedContext(@Nullable QDataContext c) {
		m_context = c;
	}

	public QDataContextFactory internalGetDataContextFactory() {
		return m_factory;
	}

	public void internalSetDataContextFactory(@Nullable QDataContextFactory s) {
		m_factory = s;
	}
}
