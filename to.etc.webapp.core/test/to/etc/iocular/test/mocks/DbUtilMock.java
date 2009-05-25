package to.etc.iocular.test.mocks;

public class DbUtilMock {
	static public DataContextMock			createContext() {
		return new DataContextMock();
	}

	static public void						discardContext(final DataContextMock dcm) {
		dcm.decrement();
	}

}
