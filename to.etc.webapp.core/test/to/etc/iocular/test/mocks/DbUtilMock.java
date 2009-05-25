package to.etc.iocular.test.mocks;

import to.etc.webapp.query.*;

public class DbUtilMock {
	static public QDataContext			createContext() {
		return new DataContextMock();
	}

	static public void discardContext(final DataContextMock dcm) {
		dcm.decrement();
	}

}
