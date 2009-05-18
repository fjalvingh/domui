package to.etc.iocular.test;

import org.junit.*;

import to.etc.iocular.container.*;
import to.etc.iocular.def.*;
import to.etc.iocular.test.mocks.*;
import to.etc.webapp.query.*;

public class TestBasicConfigs {
	@BeforeClass
	static public void init() {
		QContextManager.initialize(new QDataContextSource() {
			public void releaseDataContext(final QDataContext dc) {
			}

			public QDataContext getDataContext() throws Exception {
				return new DataContextMock();
			}
		});
	}

	private <T>	T	make(final BasicContainerBuilder b, final Class<T> clz, final Object... prams) throws Exception {
		ContainerDefinition	cd	= b.createDefinition();
		BasicContainer	bc	= new BasicContainer(cd, null);
		bc.start();

		bc.dump(clz);

		return bc.getObject(clz);
	}

	@Test(expected=IocContainerException.class)
	public void		testUnsetParameter() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register().type(PlannerMock.class);

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");
		QDataContext	dc = make(b, QDataContext.class);
		Assert.assertNull(dc);
	}

	@Test
	public void		testComplexConfig() throws Exception {
		System.out.println("---- complex config test ----");
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register().type(PlannerMock.class);

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Dump data
		ContainerDefinition	cd	= b.createDefinition();
		BasicContainer	bc	= new BasicContainer(cd, null);
		bc.start();
		bc.setParameter(new PageMock());

		bc.dump(PageMock.class);
		bc.dump(QDataContext.class);

		QDataContext dc = bc.getObject(QDataContext.class);
		System.out.println("QDataContext="+dc);
	}

	@Test
	public void		testInstantConfig() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register().type(PlannerMock.class);

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Must be able to create a simple object without rulez.
		PlannerMock	pm	= make(b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}

	@Test
	public void		testPlannerMockWithoutInj() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");
		b.register().type(PlannerMock.class);
		PlannerMock	pm	= make(b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}


}
