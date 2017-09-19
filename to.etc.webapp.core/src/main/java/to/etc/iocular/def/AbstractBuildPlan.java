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
package to.etc.iocular.def;

import java.io.*;
import java.util.*;

import to.etc.iocular.ioccontainer.*;
import to.etc.util.*;

/**
 * Internal abstract base for most build plans. It only implements the property injection part
 * of creating an object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 18, 2009
 */
abstract public class AbstractBuildPlan implements BuildPlan {
	@Override
	abstract public Object getObject(BasicContainer c) throws Exception;

	@Override
	abstract public boolean needsStaticInitialization();

	@Override
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

	@Override
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

	@Override
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

	@Override
	public boolean hasDestructors() {
		return m_destroyList != null && m_destroyList.length > 0;
	}

	@Override
	public void start(final BasicContainer bc, final Object self) throws Exception {
		if(m_startList == null)
			return;
		for(MethodInvoker m : m_startList) {
			m.invoke(bc, self);
		}
	}
}
