package to.etc.iocular.test.mocks;

import to.etc.webapp.query.*;

public class PageMock implements IQContextContainer {
	public QDataContextSource internalGetContextSource() {
		return null;
	}

	public QDataContext internalGetSharedContext() {
		return null;
	}

	public void internalSetContextSource(final QDataContextSource s) {

	}

	public void internalSetSharedContext(final QDataContext c) {

	}


}
