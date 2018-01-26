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
import java.lang.reflect.*;
import java.util.*;

import to.etc.iocular.ioccontainer.*;
import to.etc.util.*;

public class BuildPlanForStaticFactory extends AbstractBuildPlan {
	/**
	 * The static factory method to invoke.
	 */
	private final Method m_method;

	/**
	 * The build plans for the method's arguments.
	 */
	private final ComponentRef[] m_argumentList;

	private MethodInvoker[] m_startList;

	private final int m_score;

	BuildPlanForStaticFactory(final Method m, final int score, final ComponentRef[] args, final List<MethodInvoker> startlist) {
		m_method = m;
		m_argumentList = args;
		m_score = score;

		if(startlist != null && startlist.size() > 0)
			m_startList = startlist.toArray(new MethodInvoker[startlist.size()]);
	}

	public int getScore() {
		return m_score;
	}

	/**
	 * Execute the plan to *get* the object from
	 * @see to.etc.iocular.ioccontainer.BuildPlan#getObject()
	 */
	@Override
	public Object getObject(final BasicContainer bc) throws Exception {
		Object[] param = new Object[m_argumentList.length];
		for(int i = m_argumentList.length; --i >= 0;) {
			param[i] = bc.retrieve(m_argumentList[i]);
		}
		Object value = m_method.invoke(null, param);
		injectProperties(value, bc);
		return value;
	}

	@Override
	public void dump(final IndentWriter iw) throws IOException {
		iw.print("Staticfactory method ");
		iw.println(m_method.toGenericString());
		if(m_argumentList.length != 0) {
			iw.println("- Method parameter build plan(s):");
			iw.inc();
			for(int i = 0; i < m_argumentList.length; i++) {
				iw.println("parameter# " + i);
				iw.inc();
				m_argumentList[i].dump(iw);
				iw.dec();
			}
			iw.dec();
		}
		super.dump(iw);
	}

	@Override
	public boolean needsStaticInitialization() {
		return m_startList != null;
	}

	/**
	 * Called as a single-time per container init only if this factory has static initializers.
	 *
	 * @see to.etc.iocular.ioccontainer.BuildPlan#staticStart(to.etc.iocular.ioccontainer.BasicContainer)
	 */
	@Override
	public void staticStart(final BasicContainer c) throws Exception {
		for(MethodInvoker miv : m_startList) {
			miv.invoke(c, null);
		}
	}
}
