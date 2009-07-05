package to.etc.iocular.test.mocks;

import to.etc.webapp.query.*;

public class PlannerMock {
	private VpUserContextMock m_userContext;

	private QDataContext m_dataContext;

	public VpUserContextMock getUserContext() {
		return m_userContext;
	}

	public void setUserContext(final VpUserContextMock userContext) {
		m_userContext = userContext;
	}

	public QDataContext getDataContext() {
		return m_dataContext;
	}

	public void setDataContext(final QDataContext dataContext) {
		m_dataContext = dataContext;
	}
}
