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

import to.etc.iocular.ioccontainer.*;
import to.etc.util.*;

/**
 * A reference to some component in a given container.
 *
 * @author jal
 * Created on Apr 9, 2007
 */
final public class ComponentRef {
	private final ComponentDef m_def;

	private final int m_containerIndex;

	ComponentRef(final ComponentDef def, final int containerIndex) {
		m_def = def;
		m_containerIndex = containerIndex;
	}

	ComponentRef(final ISelfDef def) {
		m_def = (ComponentDef) def;
		m_containerIndex = -1;
	}

	public ComponentDef getDefinition() {
		return m_def;
	}

	public boolean isSelf() {
		return m_containerIndex < 0;
	}

	public int getContainerIndex() {
		return m_containerIndex;
	}

	public void dump(final IndentWriter iw) throws IOException {
		if(isSelf()) {
			iw.println("REF:self[" + m_def.getIdent() + "]");
			return;
		}
		iw.println("REF:container[" + m_containerIndex + "] component " + m_def.getIdent() + " build plan:");
		iw.inc();
		getDefinition().getBuildPlan().dump(iw);
		iw.dec();
	}

	public BuildPlan getBuildPlan() {
		return m_def.getBuildPlan();
	}

	@Override
	public String toString() {
		if(isSelf())
			return "ref:self[" + m_def.getIdent() + "]";
		return "ref:container[" + m_containerIndex + "] component " + m_def.getIdent();
	}
}
