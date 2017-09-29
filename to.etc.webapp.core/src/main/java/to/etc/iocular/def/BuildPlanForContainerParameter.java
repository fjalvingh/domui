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
 * A build plan definition to obtain a parameter-based object from a container. This version merely obtains
 * a simple object from the container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 15, 2009
 */
public class BuildPlanForContainerParameter extends AbstractBuildPlan {
	private String m_ident;

	public BuildPlanForContainerParameter(final Class< ? > actualType, final List<String> nameList) {
		if(nameList.size() == 0)
			m_ident = actualType.getName();
		else
			m_ident = nameList.toString();
	}

	@Override
	public void dump(final IndentWriter iw) throws IOException {
		iw.println("PARAMETER " + m_ident + ": not built but must be present in Container");
		super.dump(iw);
	}

	/**
	 * Obtain this parameter from the container.
	 *
	 * @see to.etc.iocular.ioccontainer.BuildPlan#getObject(to.etc.iocular.ioccontainer.BasicContainer)
	 */
	@Override
	public Object getObject(final BasicContainer c) throws Exception {
		throw new IocContainerException(c, "The container parameter '" + m_ident + "' is not set");
	}

	@Override
	public boolean needsStaticInitialization() {
		return false;
	}

	@Override
	public void staticStart(final BasicContainer c) throws Exception {}
}
