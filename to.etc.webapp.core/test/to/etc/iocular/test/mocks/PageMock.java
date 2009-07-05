package to.etc.iocular.test.mocks;

import to.etc.webapp.query.*;

public class PageMock implements IQContextContainer {
	private QDataContext m_dc;

	private QDataContextSource m_src;

	public QDataContextSource internalGetContextSource() {
		return m_src;
	}

	public QDataContext internalGetSharedContext() {
		return m_dc;
	}

	public void internalSetContextSource(final QDataContextSource s) {
		m_src = s;
	}

	public void internalSetSharedContext(final QDataContext c) {
		m_dc = c;
	}
}
