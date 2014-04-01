/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.iocular.test;


/**
 * All tests removed because this framework should no longer be used; tests are incompatible with current set of code.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 26, 2014
 */
public class TestBasicConfigs {
//	@BeforeClass
//	static public void init() {
//		QContextManager.initialize(new QDataContextFactory() {
//			@Override
//			public QDataContext getDataContext() throws Exception {
//				return new DataContextMock();
//			}
//			@Override
//			public QEventListenerSet getEventListeners() {
//				return QEventListenerSet.EMPTY_SET;
//			}
//
//			@Override
//			public QQueryExecutorRegistry getQueryHandlerList() {
//				return QQueryExecutorRegistry.getInstance();
//			}
//		});
//	}
//
//	private <T> T make(final BasicContainerBuilder b, final Class<T> clz, final Object... prams) throws Exception {
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.dump(clz);
//		return bc.getObject(clz);
//	}
//
//	private <T> T make(final Object param, final BasicContainerBuilder b, final Class<T> clz, final Object... prams) throws Exception {
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.setParameter(param);
//		bc.dump(clz);
//		return bc.getObject(clz);
//	}
//
//	/**
//	 * Defines a container parameter but does not set it.
//	 * @throws Exception
//	 */
//	@Test(expected = IocContainerException.class)
//	public void testUnsetParameter() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class);
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//		QDataContext dc = make(b, QDataContext.class);
//		Assert.assertNull(dc);
//	}
//
//	/**
//	 * Defines a container parameter but does not set it.
//	 * @throws Exception
//	 */
//	@Test(expected = IocContainerException.class)
//	public void testParameterConfig2() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class);
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//		make(null, b, QDataContext.class);
//	}
//
//	/**
//	 * Does not define a parameter but sets it anyway, must exception.
//	 * @throws Exception
//	 */
//	@Test(expected = IocContainerException.class)
//	public void testParamConfig1() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Dump data
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.setParameter(new PageMock());
//	}
//
//	@Test
//	public void testComplexConfig() throws Exception {
//		System.out.println("---- complex config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class);
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Dump data
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.setParameter(new PageMock());
//
//		bc.dump(PageMock.class);
//		bc.dump(QDataContext.class);
//
//		QDataContext dc = bc.getObject(QDataContext.class);
//		System.out.println("QDataContext=" + dc);
//	}
//
//	@Test
//	public void testSingleton1() throws Exception {
//		System.out.println("---- singleton1 config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().implement(QDataContext.class).factory(DbUtilMock.class, "createContext");
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.dump(QDataContext.class);
//		QDataContext dc = bc.getObject(QDataContext.class); // CREATE an instance
//
//		Assert.assertEquals("Allocation count must be 1", 1, ((DataContextMock) dc).testGetUseCount());
//
//		//-- Multiple requests for the same resource MUST return the same resource
//		for(int i = 0; i < 10; i++) {
//			QDataContext d2 = bc.getObject(QDataContext.class);
//			Assert.assertEquals("Singleton object must return same instance for every getObject", d2, dc);
//			Assert.assertEquals("Allocation count must be 1 for every getObject", 1, ((DataContextMock) d2).testGetUseCount());
//		}
//	}
//
//	/**
//	 * Check that a method to call on an object actually exists.
//	 * @throws Exception
//	 */
//	@Test(expected = IocConfigurationException.class)
//	public void testDestroyMethod1() throws Exception {
//		System.out.println("---- destroyMethod1 config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().implement(QDataContext.class).factory(DbUtilMock.class, "createContext").destroy(DbUtilMock.class, "discardaContext");
//		b.createDefinition();
//	}
//
//	/**
//	 * Destructor method called must have a compatible parameter with $self.
//	 * @throws Exception
//	 */
//	@Test(expected = IocConfigurationException.class)
//	public void testDestroyMethod2() throws Exception {
//		System.out.println("---- destroyMethod2 config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().implement(QDataContext.class).factory(DbUtilMock.class, "createContext").destroy(DbUtilMock.class, "badDiscardContext");
//		b.createDefinition();
//	}
//
//	/**
//	 * Add an object which defines a DESTROY method on a factory class; make sure it gets called at destroy time.
//	 * @throws Exception
//	 */
//	@Test
//	public void testDestroyMethod3() throws Exception {
//		System.out.println("---- destroyMethod3 config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().implement(QDataContext.class).factory(DbUtilMock.class, "createContext").destroy(DbUtilMock.class, "discardContext");
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.dump(QDataContext.class);
//		QDataContext dc = bc.getObject(QDataContext.class); // CREATE an instance
//
//		Assert.assertEquals("Allocation count must be 1", 1, ((DataContextMock) dc).testGetUseCount());
//
//		//-- Destroy the thing.
//		bc.destroy();
//
//		//-- Now the singleton must have been destroyed once...
//		Assert.assertEquals("Singleton must have been destroyed only once", 0, ((DataContextMock) dc).testGetUseCount());
//	}
//
//	/**
//	 * Define a destroy method on the created object itself, like session.close().
//	 * @throws Exception
//	 */
//	@Test
//	public void testDestroyMethod4() throws Exception {
//		System.out.println("---- destroyMethod4 config test ----");
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().implement(QDataContext.class).factory(DbUtilMock.class, "createContext").destroy("commit") // We use 'commit' instead of 'close' because QDataContext does not have a close method, so we fake stuff a bit here..
//		;
//		ContainerDefinition cd = b.createDefinition();
//		BasicContainer bc = new BasicContainer(cd, null);
//		bc.start();
//		bc.dump(QDataContext.class);
//		QDataContext dc = bc.getObject(QDataContext.class); // CREATE an instance
//
//		Assert.assertEquals("Allocation count must be 1", 1, ((DataContextMock) dc).testGetUseCount());
//
//		//-- Destroy the thing.
//		bc.destroy();
//
//		//-- Now the singleton must have been destroyed once...
//		Assert.assertEquals("Singleton must have been destroyed only once", 0, ((DataContextMock) dc).testGetUseCount());
//	}
//
//	@Test
//	public void testInstantConfig() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class);
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Must be able to create a simple object without rulez.
//		PlannerMock pm = make(b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
//
//	@Test
//	public void testPlannerMockWithoutInj() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//		b.register().type(PlannerMock.class);
//		PlannerMock pm = make(b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
//
//	@Test(expected = IocConfigurationException.class)
//	public void testProperty1() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class).setAllProperties();
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Must be able to create a simple object without rulez.
//		PlannerMock pm = make(b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
//
//	@Test
//	public void testProperty2() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class).setAllProperties();
//
//		b.register().type(VpUserContextMock.class)
//		//			.literal(new VpUserContextMock())
//		;
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Must be able to create a simple object without rulez.
//		PlannerMock pm = make(new PageMock(), b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
//
//	/**
//	 * Bad config: property cannot be set.
//	 * @throws Exception
//	 */
//	@Test(expected = IocConfigurationException.class)
//	public void testProperty3() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class).setAllProperties();
//
//		//		b.register()
//		//			.type(VpUserContextMock.class)
//		//		;
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Must be able to create a simple object without rulez.
//		PlannerMock pm = make(new PageMock(), b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
//
//	/**
//	 * Missing property but thats okay, were in set only known props mode.
//	 * @throws Exception
//	 */
//	@Test
//	public void testProperty4() throws Exception {
//		BasicContainerBuilder b = BasicContainerBuilder.createBuilder("root");
//
//		//-- Container Parameters
//		b.register().parameter(PageMock.class).implement(IQContextContainer.class);
//		b.register().type(PlannerMock.class).setKnownProperties();
//
//		//		b.register()
//		//			.type(VpUserContextMock.class)
//		//		;
//
//		//-- Register the factory for creating a QDataContext.
//		b.register().factory(QContextManager.class, "getContext");
//
//		//-- Must be able to create a simple object without rulez.
//		PlannerMock pm = make(new PageMock(), b, PlannerMock.class);
//		System.out.println("PlannerMock: " + pm);
//	}
}
