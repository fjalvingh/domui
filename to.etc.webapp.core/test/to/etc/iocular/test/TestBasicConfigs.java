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
	private <T>	T	make(final Object param, final BasicContainerBuilder b, final Class<T> clz, final Object... prams) throws Exception {
		ContainerDefinition	cd	= b.createDefinition();
		BasicContainer	bc	= new BasicContainer(cd, null);
		bc.start();
		bc.setParameter(param);
		bc.dump(clz);
		return bc.getObject(clz);
	}

	/**
	 * Defines a container parameter but does not set it.
	 * @throws Exception
	 */
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

	/**
	 * Defines a container parameter but does not set it.
	 * @throws Exception
	 */
	@Test(expected=IocContainerException.class)
	public void		testParameterConfig2() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register().type(PlannerMock.class);

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");
		make(null, b, QDataContext.class);
	}

	/**
	 * Does not define a parameter but sets it anyway, must exception.
	 * @throws Exception
	 */
	@Test(expected=IocContainerException.class)
	public void		testParamConfig1() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Dump data
		ContainerDefinition	cd	= b.createDefinition();
		BasicContainer	bc	= new BasicContainer(cd, null);
		bc.start();
		bc.setParameter(new PageMock());
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

	@Test(expected=IocConfigurationException.class)
	public void		testProperty1() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register()
			.type(PlannerMock.class)
			.setAllProperties()
		;

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Must be able to create a simple object without rulez.
		PlannerMock	pm	= make(b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}

	@Test
	public void		testProperty2() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register()
			.type(PlannerMock.class)
			.setAllProperties()
		;

		b.register()
			.type(VpUserContextMock.class)
//			.literal(new VpUserContextMock())
		;

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Must be able to create a simple object without rulez.
		PlannerMock	pm	= make(new PageMock(), b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}

	/**
	 * Bad config: property cannot be set.
	 * @throws Exception
	 */
	@Test(expected=IocConfigurationException.class)
	public void		testProperty3() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register()
			.type(PlannerMock.class)
			.setAllProperties()
		;

//		b.register()
//			.type(VpUserContextMock.class)
//		;

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Must be able to create a simple object without rulez.
		PlannerMock	pm	= make(new PageMock(), b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}

	/**
	 * Missing property but thats okay, were in set only known props mode.
	 * @throws Exception
	 */
	@Test
	public void		testProperty4() throws Exception {
		BasicContainerBuilder	b	= BasicContainerBuilder.createBuilder("root");

		//-- Container Parameters
		b.register()
			.parameter(PageMock.class)
			.implement(IQContextContainer.class)
		;
		b.register()
			.type(PlannerMock.class)
			.setKnownProperties()
		;

//		b.register()
//			.type(VpUserContextMock.class)
//		;

		//-- Register the factory for creating a QDataContext.
		b.register().factory(QContextManager.class, "getContext");

		//-- Must be able to create a simple object without rulez.
		PlannerMock	pm	= make(new PageMock(), b, PlannerMock.class);
		System.out.println("PlannerMock: "+pm);
	}
}
