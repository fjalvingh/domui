package to.etc.iocular.test.mocks;

import to.etc.webapp.query.*;

public class DbUtilMock {
	static public QDataContext			createContext() {
		return new DataContextMock();
	}

	static public void discardContext(final QDataContext dcm) {
		((DataContextMock)dcm).decrement();
	}
	/**
	 * Used to check destroy method parameter checking. This is an invalid destroyer.
	 * @param dcm
	 */
	static public void badDiscardContext(final DataContextMock dcm) {
	}
}
