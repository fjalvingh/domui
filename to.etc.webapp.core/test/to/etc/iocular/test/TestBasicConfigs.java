package to.etc.iocular.test;

import org.junit.*;

import to.etc.iocular.container.*;
import to.etc.iocular.def.*;
import to.etc.iocular.test.mocks.*;
import to.etc.webapp.query.*;

public class TestBasicConfigs {
	private <T>	T	make(final BasicContainerBuilder b, final Class<T> clz, final Object... prams) throws Exception {
		ContainerDefinition	cd	= b.createDefinition();
		BasicContainer	bc	= new BasicContainer(cd, null);
		bc.start();
		return bc.getObject(clz);
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
}
