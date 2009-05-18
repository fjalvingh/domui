package to.etc.iocular.def;

import java.io.*;
import java.util.*;

import to.etc.iocular.container.*;
import to.etc.util.*;

/**
 * Internal abstract base for most build plans. It only implements the property injection part
 * of creating an object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 18, 2009
 */
abstract public class AbstractBuildPlan implements BuildPlan {
	abstract public void dump(IndentWriter iw) throws IOException;
	abstract public Object getObject(BasicContainer c) throws Exception;
	abstract public boolean needsStaticInitialization();
	abstract public void staticStart(BasicContainer c) throws Exception;

	private List<PropertyInjector>			m_injectorList;

	public List<PropertyInjector> getInjectorList() {
		return m_injectorList;
	}
	public void setInjectorList(final List<PropertyInjector> injectorList) {
		m_injectorList = injectorList;
	}

	protected void	injectProperties(final Object instance, final BasicContainer bc) throws Exception {
		for(PropertyInjector pi: m_injectorList) {
			Object value	= bc.retrieve(pi.getRef());
			pi.getSetter().invoke(instance, value);
		}
	}
	protected void	dumpSetters(IndentWriter iw) throws IOException {
		if(m_injectorList.size() == 0)
			return;
		iw.println("Property Injectors:");
		iw.inc();
		for(PropertyInjector pij: m_injectorList) {
			iw.println("method: "+pij.getSetter().getName()+" refers "+pij.getRef());
		}
		
	}
}
