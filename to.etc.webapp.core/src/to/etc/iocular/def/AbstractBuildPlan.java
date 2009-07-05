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
	abstract public Object getObject(BasicContainer c) throws Exception;

	abstract public boolean needsStaticInitialization();

	abstract public void staticStart(BasicContainer c) throws Exception;

	/** Injector list for setters that are to be set. */
	private List<PropertyInjector> m_injectorList;

	/**
	 * The list of START methods to invoke after construction of this object.
	 */
	private MethodInvoker[] m_startList;

	/**
	 * The list of DESTROY methods to invoke after construction of this object.
	 */
	private MethodInvoker[] m_destroyList;

	public List<PropertyInjector> getInjectorList() {
		return m_injectorList;
	}

	public void setInjectorList(final List<PropertyInjector> injectorList) {
		m_injectorList = injectorList;
	}

	public void dump(final IndentWriter iw) throws IOException {
		internalDumpSetters(iw);
		internalDumpStartStop(iw);
	}

	protected void injectProperties(final Object instance, final BasicContainer bc) throws Exception {
		for(PropertyInjector pi : m_injectorList) {
			Object value = bc.retrieve(pi.getRef());
			pi.getSetter().invoke(instance, value);
		}
	}

	protected void internalDumpSetters(final IndentWriter iw) throws IOException {
		if(m_injectorList.size() == 0)
			return;
		iw.println("Property Injectors:");
		iw.inc();
		for(PropertyInjector pij : m_injectorList) {
			iw.println("method: " + pij.getSetter().getName() + " refers " + pij.getRef());
		}
	}

	protected void internalDumpStartStop(final IndentWriter iw) throws IOException {
		if(m_startList == null)
			iw.println("No START methods defined.");
		else {
			iw.println("Start method(s):");
			iw.inc();
			int ix = 0;
			for(MethodInvoker mi : m_startList) {
				iw.print("#" + ix + ": ");
				mi.dump(iw);
				ix++;
			}
		}

		if(m_destroyList == null)
			iw.println("No DESTROY methods defined.");
		else {
			iw.println("Destroy method(s):");
			iw.inc();
			int ix = 0;
			for(MethodInvoker mi : m_destroyList) {
				iw.print("#" + ix + ": ");
				mi.dump(iw);
				ix++;
			}
		}
	}

	public void destroy(final BasicContainer bc, final Object self) {
		if(m_destroyList == null)
			return;
		for(MethodInvoker m : m_destroyList) {
			try {
				m.invoke(bc, self);
			} catch(Exception x) {
				System.err.println("Exception while trying to destroy instance=" + self);
				x.printStackTrace();
			}
		}
	}

	public void setDestroyList(final MethodInvoker[] destroyList) {
		m_destroyList = destroyList;
	}

	public void setStartList(final MethodInvoker[] startList) {
		m_startList = startList;
	}

	public boolean hasDestructors() {
		return m_destroyList != null && m_destroyList.length > 0;
	}

	public void start(final BasicContainer bc, final Object self) throws Exception {
		if(m_startList == null)
			return;
		for(MethodInvoker m : m_startList) {
			m.invoke(bc, self);
		}
	}
}
