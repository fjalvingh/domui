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

import to.etc.iocular.ioccontainer.*;
import to.etc.util.*;

/**
 * A build plan to call a constructor.
 *
 * @author jal
 * Created on Mar 28, 2007
 */
final public class BuildPlanForConstructor extends AbstractBuildPlan {
	private final int m_score;

	private final Constructor< ? > m_constructor;

	private final ComponentRef[] m_actuals;

	public BuildPlanForConstructor(final Constructor< ? > constructor, final int score, final ComponentRef[] actuals) {
		m_constructor = constructor;
		m_score = score;
		m_actuals = actuals;
	}

	public BuildPlanForConstructor(final Constructor< ? > constructor, final int score) {
		this(constructor, score, BuildPlan.EMPTY_PLANS);
	}

	@Override
	public Object getObject(final BasicContainer bc) throws Exception {
		Object[] param = new Object[m_actuals.length];
		for(int i = m_actuals.length; --i >= 0;) {
			param[i] = bc.retrieve(m_actuals[i]);
		}

		//-- Construct a new instance,
		Object inst = m_constructor.newInstance(param);
		injectProperties(inst, bc);
		return inst;
	}

	public int getScore() {
		return m_score;
	}

	@Override
	public void dump(final IndentWriter iw) throws IOException {
		iw.print("InstanceConstructor ");
		iw.print(m_constructor.toGenericString());
		iw.print(" (score ");
		iw.print(Integer.toString(m_score));
		iw.println(")");
		if(m_actuals.length != 0) {
			iw.println("- Constructor parameter build plan(s):");
			iw.inc();
			for(int i = 0; i < m_actuals.length; i++) {
				iw.println("constructor parameter# " + i);
				iw.inc();
				if(m_actuals[i] == null)
					iw.println("!?!?!?! null BuildPlan!!??!");
				else
					m_actuals[i].dump(iw);
				iw.dec();
			}
			iw.dec();
		}
		super.dump(iw);
	}

	@Override
	public boolean needsStaticInitialization() {
		return false;
	}

	@Override
	public void staticStart(final BasicContainer c) throws Exception {}
}
